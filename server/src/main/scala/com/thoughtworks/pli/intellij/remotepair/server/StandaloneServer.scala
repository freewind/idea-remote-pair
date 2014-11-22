package com.thoughtworks.pli.intellij.remotepair.server

object StandaloneServer extends App {

  val port = 8888
  (new Server).start(port)

  println("Remote pair server is started on: " + port)

}
