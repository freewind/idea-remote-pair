package com.thoughtworks.pli.intellij.remotepair

trait PublishEvents extends AppConfig {
  this: ClientContextHolder =>

  def publishEvent(event: PairEvent) = {
    println("*********** publish event: " + event)
    println("#### context: " + context)
    context.foreach { x =>
      val line = event.toMessage
      x.writeAndFlush(line)
      println("!!!!!!!!!!!! write to server: " + line)
    }
  }

}
