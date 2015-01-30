package com.thoughtworks.pli.remotepair.idea.core

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.protocol.{CloseTabEvent, OpenTabEvent}
import com.thoughtworks.pli.remotepair.idea.listeners._
import org.jetbrains.annotations.NotNull

trait MyFileEditorManagerAdapter extends PublishEvents with DocumentListenerSupport with CaretListenerSupport with SelectionListenerSupport with AppLogger with PublishVersionedDocumentEvents {
  this: CurrentProjectHolder =>

  def createFileEditorManager() = new FileEditorManagerAdapter() {
    val listeners: Seq[ListenerManageSupport[_]] = Seq(
      createDocumentListener(),
      createCaretListener(),
      createSelectionListener())

    override def fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
      log.info("<event> file opened: " + file)
      publishCreateDocumentEvent(file)
    }

    override def fileClosed(source: FileEditorManager, file: VirtualFile) {
      log.info("<event> file closed: " + file)
    }

    override def selectionChanged(event: FileEditorManagerEvent) {
      val oldEditor = Option(event.getOldEditor)
      val newEditor = Option(event.getNewEditor)

      oldEditor match {
        case Some(x: TextEditor) => listeners.foreach(_.removeListener(x.getEditor))
        case _ =>
      }
      newEditor match {
        case Some(x: TextEditor) => listeners.foreach(_.addListener(x.getEditor, event.getNewFile, event.getManager.getProject))
        case _ =>
      }

      val oldFile = Option(event.getOldFile)
      val newFile = Option(event.getNewFile)

      log.info(s"<event> file selection changed: $oldFile -> $newFile")

      oldFile.flatMap(currentProject.getRelativePath).foreach(p => publishEvent(CloseTabEvent(p)))
      newFile.flatMap(currentProject.getRelativePath).foreach(p => publishEvent(OpenTabEvent(p)))
    }

  }
}
