package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import org.jetbrains.annotations.NotNull
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.listeners.{DocumentListenerSupport, CaretListenerSupport, SelectionListenerSupport, ScrollingListenerSupport}

trait MyFileEditorManagerAdapter extends PublishEvents {
  this: ClientContextHolder with DocumentListenerSupport =>

  def createFileEditorManager() = new FileEditorManagerAdapter() {
    val listeners = Seq(
      createDocumentListener(),
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
        case Some(x: TextEditor) => listeners.foreach(_.addListener(x.getEditor, event.getNewFile, event.getManager.getProject))
        case _ =>
      }

      val oldFile = Option(event.getOldFile)
      val newFile = Option(event.getNewFile)

      System.out.println(s"########## file selection changed: $oldFile -> $newFile")

      oldFile.foreach(f => publishEvent(leaveTab(mypath(f.getPath, event.getManager.getProject))))
      newFile.foreach(f => publishEvent(openTab(mypath(f.getPath, event.getManager.getProject))))
    }

    private def mypath(f: String, project: Project) = {
      val sss = f.replace(project.getBasePath, "")
      println("######## path: " + sss)
      sss
    }

    private def leaveTab(f: String) = LeaveTabEvent(f)

    private def openTab(f: String) = OpenTabEvent(f)
  }
}