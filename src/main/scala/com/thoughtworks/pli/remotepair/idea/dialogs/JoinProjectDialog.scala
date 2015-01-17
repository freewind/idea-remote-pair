package com.thoughtworks.pli.remotepair.idea.dialogs

import java.awt.event.{ActionEvent, ActionListener}

import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.AppSettingsProperties

import scala.collection.JavaConversions._

class JoinProjectDialog(override val currentProject: RichProject, remoteServerAddress: ServerAddress)
  extends _JoinProjectDialog
  with PublishEvents with InvokeLater with CurrentProjectHolder with AppSettingsProperties with PublishVersionedDocumentEvents {
  dialog =>

  getExistingProjects.foreach(addExistingProject)
  init()
  restoreInputValues()

  val client = new Client(remoteServerAddress)
  client.connect(new MyChannelHandler {
    override def channelActive(conn: Connection): Unit = {

    }
    override def channelInactive(conn: Connection): Unit = {
      dialog.dispose()
    }
    override def channelRead(conn: Connection, event: PairEvent): Unit = event match {
      case JoinedToProjectEvent(projectName, clientName) =>
        currentProject.getOpenedFiles.foreach(publishCreateDocumentEvent)
        dialog.dispose()
      case ProjectOperationFailed(msg) => showErrorMessage(msg)
      case _ =>
    }
    override def exceptionCaught(conn: Connection, cause: Throwable): Unit = {
      showErrorMessage(cause.toString)
    }
  })


  btnOk.addActionListener(new ActionListener() {
    override def actionPerformed(actionEvent: ActionEvent): Unit = {
      storeInputValues()
      invokeLater {
        try {
          getSelectedOrCreatedProjectName match {
            case Some(Left(p)) => client.publish(new JoinProjectRequest(p, txtClientName.getText))
            case Some(Right(p)) => client.publish(new CreateProjectRequest(p, txtClientName.getText))
            case _ => showErrorMessage("No valid project name")
          }
        } catch {
          case e: Throwable => currentProject.showErrorDialog("Error", e.toString)
        }
      }
    }
  })

  private def getExistingProjects: Seq[ProjectWithMemberNames] = currentProject.serverStatus.toSeq
    .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))

  private def storeInputValues(): Unit = appProperties.clientName = txtClientName.getText
  private def restoreInputValues(): Unit = txtClientName.setText(appProperties.clientName)
  private def getSelectedOrCreatedProjectName: Option[Either[String, String]] = {
    projectRadios.find(_.isSelected).map(_.getText) match {
      case Some(name) => Some(Left(name))
      case _ => Some(txtNewProjectName.getText.trim).filter(_.nonEmpty).map(Right.apply)
    }
  }
}

case class ProjectWithMemberNames(projectName: String, memberNames: Seq[String])
