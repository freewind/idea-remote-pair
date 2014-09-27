package com.thoughtworks.pli.intellij.remotepair

import com.intellij.openapi.fileEditor._
import com.intellij.openapi.vfs._
import org.jetbrains.annotations.NotNull
import com.intellij.util.messages.MessageBusConnection
import com.intellij.openapi.components.ProjectComponent
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.impl.BulkVirtualFileListenerAdapter
import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.util.{UserDataHolder, Key}
import com.intellij.openapi.editor.Editor

class RemotePairProjectComponent(project: Project) extends ProjectComponent {

  override def initComponent() {
    System.out.println("##### RemotePairProjectComponent.initComponent")
    System.out.println("## project: " + project)
  }

  override def disposeComponent() {
  }

  override def getComponentName = "RemotePairProjectComponent"

  override def projectOpened() {
    System.out.println("########## projectOpened")
    val connection: MessageBusConnection = project.getMessageBus.connect(project)
    connection.subscribe(FileEditorManagerListener.FILE_EDITOR_MANAGER, MyFileEditorManagerAdapter)
    connection.subscribe(VirtualFileManager.VFS_CHANGES, new BulkVirtualFileListenerAdapter(MyVirtualFileAdapter))
    System.out.println("########## added listeners")
  }

  override def projectClosed() {
  }


}

object MyFileEditorManagerAdapter extends FileEditorManagerAdapter with DocumentListenerSupport {
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
      case Some(x: TextEditor) => removeDocumentListener(x.getEditor)
      case _ =>
    }
    newEditor match {
      case Some(x: TextEditor) => addDocumentListener(x.getEditor)
      case _ =>
    }

    val oldFile = event.getOldFile
    val newFile = event.getNewFile
    System.out.println(s"########## file selection changed: $oldFile -> $newFile")
  }

  private def addDocumentListener(editor: Editor) {
    getDocumentListener(editor) match {
      case None =>
        val listener = createNewListener()
        editor.getDocument.addDocumentListener(listener)
        putDocumentListener(editor, listener)
      case _ =>
    }
  }

  private def createNewListener() = {
    new DocumentListener {
      override def documentChanged(event: DocumentEvent) {
        println("## documentChanged: " + event)
      }

      override def beforeDocumentChange(event: DocumentEvent) {
        println("## beforeDocumentChanged: " + event)
      }
    }
  }


}

object MyVirtualFileAdapter extends VirtualFileAdapter {
  override def fileDeleted(event: VirtualFileEvent) {
    println("### file deleted: " + event.getFile)
  }

  override def fileCreated(event: VirtualFileEvent) {
    println("### file created: " + event.getFile)
  }

  override def fileMoved(event: VirtualFileMoveEvent) {
    println("### file moved: " + event.getFile)
  }

  override def propertyChanged(event: VirtualFilePropertyEvent) {
    println("### file property changed: " + event.getFile)
  }

  override def fileCopied(event: VirtualFileCopyEvent) {
    println("### file copied: " + event.getFile)
  }

  override def contentsChanged(event: VirtualFileEvent) {
    println("### contents changed: " + event.getFile)
  }

}

trait DocumentListenerSupport {
  private val DocumentListener = new Key[DocumentListener]("remote_pair.document")

  def removeDocumentListener(editor: Editor) {
    getDocumentListener(editor).foreach { listener =>
      editor.getDocument.removeDocumentListener(listener)
      editor.putUserData(DocumentListener, null)
    }
  }

  def getDocumentListener(editor: UserDataHolder): Option[DocumentListener] = {
    Option(editor.getUserData(DocumentListener))
  }

  def putDocumentListener(editor: UserDataHolder, listener: DocumentListener) {
    editor.putUserData(DocumentListener, listener)
  }

}


