package example

import org.graalvm.polyglot._

trait Thenable {
  def `then`(onResolve: Value, onReject: Value): Unit
}
