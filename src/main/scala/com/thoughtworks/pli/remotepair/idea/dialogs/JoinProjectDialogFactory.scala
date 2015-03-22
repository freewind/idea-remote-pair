package com.thoughtworks.pli.remotepair.idea.dialogs

import com.intellij.openapi.diagnostic.Logger
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.ClientNameInGlobalStorage
import com.thoughtworks.pli.remotepair.idea.utils.InvokeLater

import scala.collection.JavaConversions._

object JoinProjectDialogFactory {
  type JoinProjectDialog = JoinProjectDialogFactory#create
}

case class JoinProjectDialogFactory(invokeLater: InvokeLater, watchFilesDialogFactory: WatchFilesDialogFactory, pairEventListeners: PairEventListeners, logger: Logger, publishEvent: PublishEvent, showServerError: ShowServerError, getExistingProjects: GetExistingProjects, clientNameInGlobalStorage: ClientNameInGlobalStorage, getProjectWindow: GetProjectWindow, getServerWatchingFiles: GetServerWatchingFiles) {
  factory =>

  case class create() extends _JoinProjectDialog with JDialogSupport {
    def invokeLater = factory.invokeLater
    override def getProjectWindow = factory.getProjectWindow
    def pairEventListeners = factory.pairEventListeners

    onWindowOpened(initDialog())
    monitorReadEvent {
      case JoinedToProjectEvent(projectName, clientName) => chooseIgnoreFiles()
      case ProjectOperationFailed(msg) => showErrorMessage(msg)
    }
    onClick(okButton)(publishProjectEvent())

    private def initDialog(): Unit = {
      getExistingProjects().foreach(generateRadio)
      init()
      clientNameTextField.setText(clientNameInGlobalStorage.load())
    }

    private def chooseIgnoreFiles(): Unit = {
      this.dispose()
      if (getServerWatchingFiles().isEmpty) {
        watchFilesDialogFactory.create().showOnCenter()
      }
    }

    private def publishProjectEvent() = {
      clientNameInGlobalStorage.save(clientNameTextField.getText)
      errorMessageLabel.setVisible(false)
      try {
        getSelectedOrCreatedProjectName match {
          case Some(ExistingProjectName(p)) => publishEvent(new JoinProjectRequest(p, clientNameTextField.getText))
          case Some(NewProjectName(p)) => publishEvent(new CreateProjectRequest(p, clientNameTextField.getText))
          case _ => showErrorMessage("No valid project name")
        }
      } catch {
        case e: Throwable =>
          errorMessageLabel.setVisible(true)
          errorMessageLabel.setText(e.toString)
      }
    }

    private def getSelectedOrCreatedProjectName: Option[ProjectName] = {
      projectRadios.find(_.isSelected).map(_.getText) match {
        case Some(name) => Some(ExistingProjectName(name))
        case _ => Some(newProjectNameTextField.getText.trim).filter(_.nonEmpty).map(NewProjectName.apply)
      }
    }
  }

  sealed trait ProjectName
  case class ExistingProjectName(name: String) extends ProjectName
  case class NewProjectName(name: String) extends ProjectName

}

