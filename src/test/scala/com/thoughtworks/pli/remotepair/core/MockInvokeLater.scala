package com.thoughtworks.pli.remotepair.core

import javax.swing.SwingUtilities

import scala.concurrent.duration._
import scala.concurrent.{Await, Promise}

class MockInvokeLater {
  self =>

  private var promises: List[Promise[_]] = Nil

  def apply[T](f: => T) = {
    val promise = Promise[T]()
    promises = promise :: promises

    SwingUtilities.invokeLater(new Runnable {
      override def run(): Unit = try {
        promise.success(f)
      } catch {
        case e: Throwable => {
          e.printStackTrace()
          promise.failure(e)
        }
      }
    })

    new {
      def await(waitTime: Int = 1000) = self.await(waitTime)
    }
  }

  def await(waitTime: Int = 1000) = {
    def await(completeCount: Int) {
      val count = promises.length
      if (count > completeCount) {
        promises.foreach(p => Await.ready(p.future, Duration(waitTime, MILLISECONDS)))
        await(count)
      }
    }
    await(0)
  }

}
