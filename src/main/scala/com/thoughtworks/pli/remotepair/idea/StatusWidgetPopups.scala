package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, DefaultActionGroup}
import com.thoughtworks.pli.intellij.remotepair.protocol.{CaretSharingModeRequest, ParallelModeRequest}
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.core.MySystem
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.{MyProject, MyIde}
import com.thoughtworks.pli.remotepair.idea.actions.{ConnectServerAction, StartServerAction, WatchFilesAction}
import com.thoughtworks.pli.remotepair.idea.dialogs.{JDialogSupport, SyncFilesForMasterDialog, SyncFilesForSlaveDialog}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StatusWidgetPopups(currentProject: MyProject, myClient: MyClient, myPlatform: MyIde, mySystem: MySystem,
                         syncFilesForMasterDialogFactory: SyncFilesForMasterDialog.Factory, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory,
                         copyProjectUrlToClipboard: CopyProjectUrlToClipboard) {

  import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget._

  def createActionGroup(): DefaultActionGroup = {
    val group = new DefaultActionGroup()
    if (myClient.isConnected) {
      group.addSeparator("Current project")
      showProjectMembers().foreach(group.add)
      group.add(action("Copy project url to clipboard", copyProjectUrlToClipboard()))
      group.add(new WatchFilesAction())
      group.add(action("Sync files", createSyncDialog().showOnCenter()))
      group.add(action("Disconnect", myClient.closeConnection()))

      group.addSeparator("Pair mode")
      group.addAll(createPairModeGroup(): _*)

      group.addSeparator("Readonly")
      group.add(createReadonlyAction())
    } else {
      group.add(createConnectServerAction())
    }

    group.addSeparator("Pair server")
    myClient.serverHolder.get match {
      case Some(server) =>
        group.add(createRunningServerGroup(server))
      case _ =>
        group.add(createStartServerAction())
    }

    group
  }

  def createSyncDialog(): JDialogSupport = {
    if (myClient.amIMaster) {
      syncFilesForMasterDialogFactory()
    } else {
      syncFilesForSlaveDialogFactory()
    }
  }

  def showProjectMembers() = for {
    projectName <- myClient.projectInfoData.map(_.name)
    names = myClient.allClients.map(_.name)
  } yield action(s"Members (${names.mkString(",")})", ())

  private def chosenAction(label: String, f: => Any = ()) = new AnAction("√ " + label) {
    override def actionPerformed(anActionEvent: AnActionEvent): Unit = f
  }

  def action(label: String, f: => Any) = new AnAction(label) {
    override def actionPerformed(anActionEvent: AnActionEvent): Unit = f
  }

  def createReadonlyAction(): AnAction = {
    if (myClient.isReadonlyMode) {
      chosenAction("readonly", myClient.setReadonlyMode(readonly = false))
    } else {
      action("readonly", myClient.setReadonlyMode(readonly = true))
    }
  }

  def createPairModeGroup(): Seq[AnAction] = if (myClient.isCaretSharing) {
    Seq(chosenAction(CaretSharingMode.icon),
      action(ParallelMode.icon, myClient.publishEvent(ParallelModeRequest)))
  } else {
    Seq(action(CaretSharingMode.icon, myClient.publishEvent(CaretSharingModeRequest)),
      chosenAction(ParallelMode.icon))
  }

  def createConnectServerAction() = new ConnectServerAction()

  def createRunningServerGroup(server: Server) = {
    val group = createPopupGroup()
    group.getTemplatePresentation.setText(s"Local server => ${server.host.getOrElse(mySystem.localIp)}:${server.port}")
    group.add(action("stop", myPlatform.invokeLater {
      server.close().addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture): Unit = {
          if (f.isSuccess) {
            myClient.serverHolder.set(None)
          } else {
            myPlatform.invokeLater(currentProject.showErrorDialog("Error", "Can't stop server"))
          }
        }
      })
    }))
    group
  }

  def createStartServerAction() = new StartServerAction()

  private def createPopupGroup() = new DefaultActionGroup(null, true)

}


