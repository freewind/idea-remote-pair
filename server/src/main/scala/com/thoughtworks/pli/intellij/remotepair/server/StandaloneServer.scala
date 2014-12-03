package com.thoughtworks.pli.intellij.remotepair.server

object StandaloneServer extends App {

  val port = 31415
  (new Server).start(port)

  println("Remote pair server is started on: " + port)

}
