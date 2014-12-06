//package com.thoughtworks.pli.intellij.remotepair.actions.dialogs
//
//import javax.swing.SwingUtilities
//
//import scala.concurrent.{Promise, Future}
//
//trait DialogFuture {
//  def create[T](dialog: => T): Future[T] = {
//    val p = Promise[T]()
//    SwingUtilities.invokeLater(new Runnable() {
//      override def run(): Unit = {
//        p.success(dialog)
//      }
//    })
//    p.future
//  }
//}
