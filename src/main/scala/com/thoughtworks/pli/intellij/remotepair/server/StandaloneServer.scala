package com.thoughtworks.pli.intellij.remotepair.server

import java.net.InetAddress

object StandaloneServer extends App {

  val port = 8888
  (new Server).start(port)
  println("######### server should be started on " + InetAddress.getLocalHost() + ":" + port)

}
