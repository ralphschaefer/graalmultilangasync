package example


import org.graalvm.polyglot._

import scala.jdk.FunctionConverters._

trait JsContextPool {

  import JsContextPool._

  val poolSize: Int

  val engine = Engine.newBuilder().build()

  lazy private val contexts: Array[Ctx] = (for (i <- 0 until poolSize) yield {
    val ctx = Context.newBuilder("js")
        .allowHostClassLookup(((clazz:String) => true).asJava)
        .allowHostAccess(HostAccess.ALL)
        .engine(engine)
        .build()
    ctx.getBindings("js").putMember("callAsync", ((in:String) => new TestAsync(in)).asJava)
    Ctx(
      context = ctx,
      index = i,
      inUse = false
    )
  }).toArray


  def get: Option[Ctx] = synchronized {
    val i = contexts.find(!_.inUse).map(_.index)
    // println(s"acquire lock: $i")
    i.map{index =>
      contexts(index).inUse = true
      contexts(index)
    }
  }

  def release(ctx: Ctx):Unit = synchronized {
    // println(s"release lock: ${ctx.index}")
    contexts(ctx.index).inUse = false
  }

}

object JsContextPool extends JsContextPool {
  val poolSize: Int = 100
  case class Ctx(context: Context, index: Int, var inUse: Boolean)
}