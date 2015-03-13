package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.IdeaSettingsProperties
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

import scala.collection.JavaConversions._


object JoinProjectDialogFactory {
  type JoinProjectDialog = JoinProjectDialogFactory#create
}

case class JoinProjectDialogFactory(currentProject: RichProject, invokeLater: InvokeLater, chooseIgnoreDialogFactory: ChooseIgnoreDialogFactory, appSettingsProperties: IdeaSettingsProperties, pairEventListeners: PairEventListeners, logger: Logger) {
  factory =>

  case class create() extends _JoinProjectDialog with JDialogSupport {
    def invokeLater = factory.invokeLater
    def currentProject = factory.currentProject
    def pairEventListeners = factory.pairEventListeners

    getExistingProjects.foreach(generateRadio)
    init()
    restoreInputValues()

    monitorReadEvent {
      case JoinedToProjectEvent(projectName, clientName) => {
        this.dispose()
        if (currentProject.ignoredFiles.isEmpty) {
          chooseIgnoreDialogFactory.create().showOnCenter()
        }
      }
      case ProjectOperationFailed(msg) => showErrorMessage(msg)
    }

    clickOn(okButton) {
      errorMessageLabel.setVisible(false)
      storeInputValues()
      logger.info("currentProject in login dialog: " + currentProject.hashCode())
      logger.info("currentProject.connection: " + currentProject.connection)

      currentProject.connection.foreach { conn =>
        try {
          getSelectedOrCreatedProjectName match {
            case Some(Left(p)) => conn.publish(new JoinProjectRequest(p, clientNameTextField.getText))
            case Some(Right(p)) => conn.publish(new CreateProjectRequest(p, clientNameTextField.getText))
            case _ => showErrorMessage("No valid project name")
          }
        } catch {
          case e: Throwable => currentProject.showErrorDialog("Error", e.toString)
        }
      }
    }

    private def getExistingProjects: Seq[ProjectWithMemberNames] = currentProject.serverStatus.toSeq
      .flatMap(_.projects.map(p => ProjectWithMemberNames(p.name, p.clients.map(_.name))))

    private def storeInputValues(): Unit = appSettingsProperties.clientName = clientNameTextField.getText
    private def restoreInputValues(): Unit = clientNameTextField.setText(appSettingsProperties.clientName)
    private def getSelectedOrCreatedProjectName: Option[Either[String, String]] = {
      projectRadios.find(_.isSelected).map(_.getText) match {
        case Some(name) => Some(Left(name))
        case _ => Some(newProjectNameTextField.getText.trim).filter(_.nonEmpty).map(Right.apply)
      }
    }
  }

}


case class ProjectWithMemberNames(projectName: String, memberNames: Seq[String])
