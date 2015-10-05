package com.thoughtworks.pli.remotepair.idea.listeners

import akka.actor.ActorRef
import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers.{EditorFileClosedEvent, EditorFileOpenedEvent, EditorFileTabChangedEvent}
import com.thoughtworks.pli.remotepair.idea.models.IdeaFactories
import org.jetbrains.annotations.NotNull

object MyFileEditorManager {
  type Factory = () => MyFileEditorManager
}

class MyFileEditorManager(coreActor: ActorRef, logger: PluginLogger, projectDocumentListenerFactory: ProjectDocumentListenerFactory, projectCaretListenerFactory: ProjectCaretListenerFactory, projectSelectionListenerFactory: ProjectSelectionListenerFactory,
                          ideaFactories: IdeaFactories)
  extends FileEditorManagerAdapter {

  val listenerFactories: Seq[ListenerManager[_]] = Seq(
    projectDocumentListenerFactory,
    projectCaretListenerFactory,
    projectSelectionListenerFactory)

  override def fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
    logger.info("file opened event: " + file)
    coreActor ! new EditorFileOpenedEvent(ideaFactories(file))
  }

  override def fileClosed(source: FileEditorManager, file: VirtualFile) {
    logger.info("file closed event: " + file)
    coreActor ! new EditorFileClosedEvent(ideaFactories(file))
  }

  override def selectionChanged(event: FileEditorManagerEvent): Unit = {
    logger.info(s"file selection changed: ${event.getOldFile} -> ${event.getNewFile}")

    val project = event.getManager.getProject

    val oldEditor = Option(event.getOldEditor)
    val newEditor = Option(event.getNewEditor)

    oldEditor match {
      case Some(x: TextEditor) => listenerFactories.foreach(_.removeListener(x.getEditor))
      case _ =>
    }
    newEditor match {
      case Some(x: TextEditor) => listenerFactories.foreach(_.addListener(x.getEditor, event.getNewFile, project))
      case _ =>
    }

    coreActor ! new EditorFileTabChangedEvent(Option(event.getOldFile).map(ideaFactories.apply), Option(event.getNewFile).map(ideaFactories.apply))

  }

}
