package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.listeners._
import org.jetbrains.annotations.NotNull

trait MyFileEditorManagerAdapter extends PublishEvents with DocumentListenerSupport with CaretListenerSupport with SelectionListenerSupport {
  this: CurrentProjectHolder =>

  def createFileEditorManager() = new FileEditorManagerAdapter() {
    val listeners: Seq[ListenerManageSupport[_]] = Seq(
      createDocumentListener(),
      createCaretListener(),
      createSelectionListener())

    override def fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
      println("<event> file opened: " + file)
    }

    override def fileClosed(source: FileEditorManager, file: VirtualFile) {
      System.out.println("<event> file closed: " + file)
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

      println(s"<event> file selection changed: $oldFile -> $newFile")

      newFile.foreach(f => publishEvent(openTab(currentProject.getRelativePath(f))))
    }

    private def openTab(f: String) = OpenTabEvent(f)
  }
}
