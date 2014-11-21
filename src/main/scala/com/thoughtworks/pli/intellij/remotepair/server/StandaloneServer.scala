package com.thoughtworks.pli.intellij.remotepair.server

object StandaloneServer extends App {

  val port = 9090
  (new Server).start(port)
  println("######### server should be started on: " + port)

}
