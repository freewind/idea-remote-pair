package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.client.{PublishCreateDocumentEvent, PublishEvent}
import com.thoughtworks.pli.remotepair.idea.file.GetRelativePath
import org.jetbrains.annotations.NotNull

object MyFileEditorManager {
  type Factory = () => MyFileEditorManager
}

class MyFileEditorManager(projectCaretListenerFactory: ProjectCaretListenerFactory,
                          publishCreateDocumentEvent: PublishCreateDocumentEvent,
                          projectDocumentListenerFactory: ProjectDocumentListenerFactory,
                          projectSelectionListenerFactory: ProjectSelectionListenerFactory,
                          logger: PluginLogger, publishEvent: PublishEvent, getRelativePath: GetRelativePath,
                          tabEventsLocksInProject: TabEventsLocksInProject, isReadonlyMode: IsReadonlyMode)
  extends FileEditorManagerAdapter {
  val listenerFactories: Seq[ListenerManager[_]] = Seq(
    projectDocumentListenerFactory,
    projectCaretListenerFactory,
    projectSelectionListenerFactory)

  override def fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
    logger.info("file opened event: " + file)
    publishCreateDocumentEvent(file)
  }

  override def fileClosed(source: FileEditorManager, file: VirtualFile) {
    getRelativePath(file).foreach(path => publishEvent(CloseTabEvent(path)))
    logger.info("file closed event: " + file)
  }

  override def selectionChanged(event: FileEditorManagerEvent): Unit = if (!isReadonlyMode()) {
    logger.info(s"file selection changed: ${event.getOldFile} -> ${event.getNewFile}")

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

    Option(event.getNewFile).flatMap(getRelativePath.apply).foreach { p =>
      if (tabEventsLocksInProject.unlock(p)) {
        // do nothing
      } else {
        publishEvent(OpenTabEvent(p))
      }
    }
  }

}
