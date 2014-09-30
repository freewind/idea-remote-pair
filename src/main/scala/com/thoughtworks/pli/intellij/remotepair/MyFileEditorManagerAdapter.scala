package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import org.jetbrains.annotations.NotNull

object MyFileEditorManagerAdapter extends FileEditorManagerAdapter {

  val listeners = Seq(
    new DocumentListenerSupport,
    new CaretListenerSupport,
    new SelectionListenerSupport,
    new ScrollingListenerSupport)

  override def fileOpened(@NotNull source: FileEditorManager, @NotNull file: VirtualFile) {
    System.out.println("########## file opened: " + file)
  }

  override def fileClosed(source: FileEditorManager, file: VirtualFile) {
    System.out.println("########## file closed: " + file)
  }

  override def selectionChanged(event: FileEditorManagerEvent) {
    val oldEditor = Option(event.getOldEditor)
    val newEditor = Option(event.getNewEditor)

    oldEditor match {
      case Some(x: TextEditor) => listeners.foreach(_.removeListener(x.getEditor))
      case _ =>
    }
    newEditor match {
      case Some(x: TextEditor) => listeners.foreach(_.addListener(x.getEditor))
      case _ =>
    }

    val oldFile = event.getOldFile
    val newFile = event.getNewFile
    System.out.println(s"########## file selection changed: $oldFile -> $newFile")
  }

}