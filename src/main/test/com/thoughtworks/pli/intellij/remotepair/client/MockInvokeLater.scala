package com.thoughtworks.pli.intellij.remotepair.client

import scala.concurrent.{Await, Promise}
import scala.concurrent.duration._

class MockInvokeLater {

  val promise: Promise[Unit] = Promise[Unit]()
  def apply(f: => Any) {
    java.awt.EventQueue.invokeLater(new Runnable {
      override def run(): Unit = try {
        f
        promise.success(())
      } catch {
        case e: Throwable => promise.failure(e)
      }
    })
  }

  def await() {
    Await.ready(promise.future, Duration(500, MILLISECONDS))
  }

}
