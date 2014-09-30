package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.editor.event.{VisibleAreaEvent, VisibleAreaListener, SelectionEvent, SelectionListener}
import com.intellij.openapi.util.{TextRange, Key}
import com.intellij.openapi.editor.Editor

class ScrollingListenerSupport extends ListenerManageSupport[VisibleAreaListener] {

  val key = new Key[VisibleAreaListener]("remote_pair.listeners.scrolling")

  override def createNewListener(): VisibleAreaListener = new VisibleAreaListener {
    override def visibleAreaChanged(e: VisibleAreaEvent): Unit = {
      println("########### visibleAreaChanged: " + info(e))
    }

    private def info(e: VisibleAreaEvent) = s"${e.getOldRectangle} => ${e.getNewRectangle}"
  }

  override def originRemoveListener(editor: Editor): (VisibleAreaListener) => Any = editor.getScrollingModel.removeVisibleAreaListener

  override def originAddListener(editor: Editor): (VisibleAreaListener) => Any = editor.getScrollingModel.addVisibleAreaListener

}
