package example

import example.TestAsync.interopThread
import org.graalvm.polyglot._

import java.util.concurrent.Executors
import scala.concurrent.{ExecutionContext, ExecutionContextExecutor, Future}
import scala.util.{Failure, Success, Try}

class TestAsync(input:String) extends Thenable {

  import TestAsync.RunJS
  import TestAsync.ec
  import TestAsync.{Resolve,Reject}

  override def `then`(onResolve: Value, onReject: Value): Unit = {
    Future {
      val ctx: Option[JsContextPool.Ctx] = JsContextPool.get
      val res: Option[Try[String]] = ctx.map { c =>
        Thread.sleep(1500)
        new RunJS(c.context).run(input)
      }
      res match {
        case Some(Success(r)) =>
          interopThread.enqueue(Resolve(onResolve, ctx.get.context, qr))
          // onResolve.executeVoid(r)  // cause error for multiple threads
          JsContextPool.release(ctx.get)
        case Some(Failure(exception)) =>
          interopThread.enqueue(Reject(onReject, ctx.get.context, exception))
          // onReject.executeVoid(exception)
          JsContextPool.release(ctx.get)
        case None =>
          interopThread.enqueue(Reject(onReject, ctx.get.context, new Exception("new context could not be acquired")))
          // onReject.executeVoid(new Exception("new context could not be acquired"))
      }
      Thread.sleep(5000)
    }

  }

}

object TestAsync {

  val executorService = Executors.newWorkStealingPool(4)
  implicit val ec: ExecutionContextExecutor = ExecutionContext.fromExecutor(executorService)

  class RunJS(context: Context) {
    private def parseJson(jString: String): Value = context.eval("js", "JSON").invokeMember("parse", jString)

    private def stringify(v: Value): String = context.eval("js", "JSON").invokeMember("stringify", v).asString()

    private def execute(in: Value): Value = {
      val extract = context.eval("js", "(function(o,item) {return o[item];})")
      val js = context.eval("js", "(" + extract.execute(in, "fn").asString() + ")")
      js.execute(extract.execute(in, "input"))
    }

    def run(in: String): Try[String] = Try {
      val inputWithFn = parseJson(in)
      stringify(execute(inputWithFn))
    }
  }

  trait QElem {
    val to: Value
    val ctx: Context
  }
  case class Resolve(to: Value, ctx: Context, res: String) extends QElem
  case class Reject(to: Value, ctx: Context, t: Throwable) extends QElem

  // communication thread for destination context
  // only one thread is allowed to make request to destination Context
  class InteropThread extends Thread {

    import scala.collection.mutable._

    def enqueue(e: QElem): Unit = synchronized{
      q.enqueue(e)
    }

    private val q = Queue[QElem]()

    override def run() = while (true) {
      Try(q.dequeue()) match {
        case Success(Resolve(to, ctx, res)) =>
          ctx.enter()
          to.executeVoid(res)
          ctx.leave()
        case Success(Reject(to, ctx, t)) =>
          ctx.enter()
          to.executeVoid(t)
          ctx.leave()
        case _ =>
          Thread.sleep(20)
      }
    }

  }

  lazy val interopThread = {
    val t = new InteropThread()
    t.setName("interopThread")
    t.start()
    t
  }

}

