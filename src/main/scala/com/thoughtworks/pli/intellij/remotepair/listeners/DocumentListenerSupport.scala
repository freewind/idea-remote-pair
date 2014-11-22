package com.thoughtworks.pli.intellij.remotepair.listeners

import com.intellij.openapi.editor.event.{DocumentEvent, DocumentListener}
import com.intellij.openapi.util.Key
import com.intellij.openapi.editor.Editor
import com.thoughtworks.pli.intellij.remotepair.{PublishEvents, ChangeContentEvent}
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support
import com.thoughtworks.pli.intellij.remotepair.client.{CurrentProjectHolder, ClientContextHolder}

trait DocumentListenerSupport extends PublishEvents with ClientContextHolder {
  this: CurrentProjectHolder =>

  def createDocumentListener() = new ListenerManageSupport[DocumentListener] {
    val key = new Key[DocumentListener]("remote_pair.listeners.document")

    def createNewListener(editor: Editor, file: VirtualFile, project: Project): DocumentListener = {
      def mypath(f: String, project: Project) = {
        val sss = f.replace(project.getBasePath, "")
        println("######## path: " + sss)
        sss
      }
      new DocumentListener with Md5Support {

        override def documentChanged(event: DocumentEvent) {
          println("## documentChanged: " + event)
          val summary = md5(event.getDocument.getCharsSequence.toString)
          val eee = ChangeContentEvent(mypath(file.getPath, project), event.getOffset, event.getOldFragment.toString, event.getNewFragment.toString, summary)
          publishEvent(eee)
        }

        override def beforeDocumentChange(event: DocumentEvent) {
          println("## beforeDocumentChanged: " + event)
        }
      }
    }

    override def originAddListener(editor: Editor) = editor.getDocument.addDocumentListener

    override def originRemoveListener(editor: Editor) = editor.getDocument.removeDocumentListener
  }
}
