package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{VisibleAreaEvent, VisibleAreaListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject

object ScrollingListenerFactory {
  type ScrollingListener = ScrollingListenerFactory#create
}

case class ScrollingListenerFactory(currentProject: RichProject, logger: Logger) {

  case class create() extends ListenerManageSupport[VisibleAreaListener] {

    val key = new Key[VisibleAreaListener]("remote_pair.listeners.scrolling")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): VisibleAreaListener = new VisibleAreaListener {
      override def visibleAreaChanged(e: VisibleAreaEvent): Unit = {
        logger.info("########### visibleAreaChanged: " + info(e))
      }

      private def info(e: VisibleAreaEvent) = s"${e.getOldRectangle} => ${e.getNewRectangle}"
    }

    override def originRemoveListener(editor: Editor): (VisibleAreaListener) => Any = editor.getScrollingModel.removeVisibleAreaListener

    override def originAddListener(editor: Editor): (VisibleAreaListener) => Any = editor.getScrollingModel.addVisibleAreaListener

  }

}
