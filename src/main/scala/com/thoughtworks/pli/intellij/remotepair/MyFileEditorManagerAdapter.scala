package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.listeners._
import com.thoughtworks.pli.intellij.remotepair.protocol.{OpenTabEvent, CloseTabEvent}
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

      oldFile.foreach(f => publishEvent(CloseTabEvent(currentProject.getRelativePath(f))))
      newFile.foreach(f => publishEvent(OpenTabEvent(currentProject.getRelativePath(f))))
    }

  }
}
