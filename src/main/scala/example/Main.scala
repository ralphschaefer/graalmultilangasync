package example

// https://www.graalvm.org/jdk21/reference-manual/js/
// https://www.graalvm.org/jdk21/reference-manual/js/Multithreading/
// https://medium.com/graalvm/asynchronous-polyglot-programming-in-graalvm-javascript-and-java-2c62eb02acf0

import org.graalvm.polyglot._
import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}
import scala.jdk.FunctionConverters._
import scala.util.{Failure, Success}

object Main extends App {

  val start = System.currentTimeMillis()

  val j = scala.io.Source.fromResource("js/callAsync.js").mkString("")

  val ctx = JsContextPool.get.get

  val va: Value = ctx.context.eval("js", j)

  val futureResult = Promise[String]()
  val jsAsyncFunctionPromise = va.execute()
  jsAsyncFunctionPromise
    .invokeMember("then", ((r: String) => {
      futureResult.success(r)
    }).asJava)
    .invokeMember("catch", ((r:Exception) => {
      futureResult.failure(r)
    }).asJava)


  import TestAsync.ec

  val res = Await.ready(futureResult.future, 5.seconds)
  res.onComplete {
    case Success(value) =>
      println(value)
    case Failure(exception) =>
      println("error :" + exception.getMessage)
  }

  println(s"execution time = ${(System.currentTimeMillis() - start)*1.0 / 1000 }")

}


