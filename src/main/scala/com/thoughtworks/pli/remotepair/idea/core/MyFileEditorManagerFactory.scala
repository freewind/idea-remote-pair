package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.idea.listeners._
import org.jetbrains.annotations.NotNull

case class MyFileEditorManagerFactory(projectCaretListenerFactory: ProjectCaretListenerFactory,
                                      publishCreateDocumentEvent: PublishCreateDocumentEvent,
                                      projectDocumentListenerFactory: ProjectDocumentListenerFactory,
                                      projectSelectionListenerFactory: ProjectSelectionListenerFactory,
                                      logger: Logger,
                                      publishEvent: PublishEvent,
                                      getRelativePath: GetRelativePath,
                                      tabEventsLocksInProject: TabEventsLocksInProject) {

  def create() = new FileEditorManagerAdapter() {
    val listenerFactories: Seq[ListenerManager[_]] = Seq(
      projectDocumentListenerFactory,
      projectCaretListenerFactory,
      projectSelectionListenerFactory)

    override def fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
      logger.info("<event> file opened: " + file)
      publishCreateDocumentEvent(file)
    }

    override def fileClosed(source: FileEditorManager, file: VirtualFile) {
      logger.info("<event> file closed: " + file)
    }

    override def selectionChanged(event: FileEditorManagerEvent) {
      val oldEditor = Option(event.getOldEditor)
      val newEditor = Option(event.getNewEditor)

      oldEditor match {
        case Some(x: TextEditor) => listenerFactories.foreach(_.removeListener(x.getEditor))
        case _ =>
      }
      newEditor match {
        case Some(x: TextEditor) => listenerFactories.foreach(_.addListener(x.getEditor, event.getNewFile, event.getManager.getProject))
        case _ =>
      }

      val oldFile = Option(event.getOldFile)
      val newFile = Option(event.getNewFile)

      logger.info(s"<event> file selection changed: $oldFile -> $newFile")

      oldFile.flatMap(getRelativePath.apply).foreach(p => publishEvent(CloseTabEvent(p)))
      newFile.flatMap(getRelativePath.apply).foreach { p =>
        if (tabEventsLocksInProject.unlock(p)) {
          // do nothing
        } else {
          publishEvent(OpenTabEvent(p))
        }
      }
    }

  }

}
