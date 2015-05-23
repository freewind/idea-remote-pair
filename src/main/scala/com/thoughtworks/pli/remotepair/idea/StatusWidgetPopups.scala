package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent, DefaultActionGroup}
import com.thoughtworks.pli.intellij.remotepair.protocol.{CaretSharingModeRequest, ParallelModeRequest}
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.core.client._
import com.thoughtworks.pli.remotepair.core.models.MyPlatform
import com.thoughtworks.pli.remotepair.idea.actions.{ConnectServerAction, StartServerAction, WatchFilesAction}
import com.thoughtworks.pli.remotepair.idea.dialogs.{JDialogSupport, SyncFilesForMasterDialog, SyncFilesForSlaveDialog}
import com.thoughtworks.pli.remotepair.idea.idea.ShowErrorDialog
import com.thoughtworks.pli.remotepair.idea.utils.GetLocalIp
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StatusWidgetPopups(connectedClient: ConnectedClient, myPlatform: MyPlatform, localIp: GetLocalIp,
                         syncFilesForMasterDialogFactory: SyncFilesForMasterDialog.Factory, syncFilesForSlaveDialogFactory: SyncFilesForSlaveDialog.Factory,
                         showErrorDialog: ShowErrorDialog, copyProjectUrlToClipboard: CopyProjectUrlToClipboard) {

  import com.thoughtworks.pli.remotepair.idea.statusbar.PairStatusWidget._

  def createActionGroup(): DefaultActionGroup = {
    val group = new DefaultActionGroup()
    if (connectedClient.isConnected) {
      group.addSeparator("Current project")
      showProjectMembers().foreach(group.add)
      group.add(action("Copy project url to clipboard", copyProjectUrlToClipboard()))
      group.add(new WatchFilesAction())
      group.add(action("Sync files", createSyncDialog().showOnCenter()))
      group.add(action("Disconnect", connectedClient.closeConnection()))

      group.addSeparator("Pair mode")
      group.addAll(createPairModeGroup(): _*)

      group.addSeparator("Readonly")
      group.add(createReadonlyAction())
    } else {
      group.add(createConnectServerAction())
    }

    group.addSeparator("Pair server")
    connectedClient.serverHolder.get match {
      case Some(server) =>
        group.add(createRunningServerGroup(server))
      case _ =>
        group.add(createStartServerAction())
    }

    group
  }

  def createSyncDialog(): JDialogSupport = {
    if (connectedClient.amIMaster) {
      syncFilesForMasterDialogFactory()
    } else {
      syncFilesForSlaveDialogFactory()
    }
  }

  def showProjectMembers() = for {
    projectName <- connectedClient.projectInfoData.map(_.name)
    names = connectedClient.allClients.map(_.name)
  } yield action(s"Members (${names.mkString(",")})", ())

  private def chosenAction(label: String, f: => Any = ()) = new AnAction("√ " + label) {
    override def actionPerformed(anActionEvent: AnActionEvent): Unit = f
  }

  def action(label: String, f: => Any) = new AnAction(label) {
    override def actionPerformed(anActionEvent: AnActionEvent): Unit = f
  }

  def createReadonlyAction(): AnAction = {
    if (connectedClient.isReadonlyMode) {
      chosenAction("readonly", connectedClient.setReadonlyMode(readonly = false))
    } else {
      action("readonly", connectedClient.setReadonlyMode(readonly = true))
    }
  }

  def createPairModeGroup(): Seq[AnAction] = if (connectedClient.isCaretSharing) {
    Seq(chosenAction(CaretSharingMode.icon),
      action(ParallelMode.icon, connectedClient.publishEvent(ParallelModeRequest)))
  } else {
    Seq(action(CaretSharingMode.icon, connectedClient.publishEvent(CaretSharingModeRequest)),
      chosenAction(ParallelMode.icon))
  }

  def createConnectServerAction() = new ConnectServerAction()

  def createRunningServerGroup(server: Server) = {
    val group = createPopupGroup()
    group.getTemplatePresentation.setText(s"Local server => ${server.host.getOrElse(localIp())}:${server.port}")
    group.add(action("stop", myPlatform.invokeLater {
      server.close().addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture): Unit = {
          if (f.isSuccess) {
            connectedClient.serverHolder.set(None)
          } else {
            myPlatform.invokeLater(showErrorDialog("Error", "Can't stop server"))
          }
        }
      })
    }))
    group
  }

  def createStartServerAction() = new StartServerAction()

  private def createPopupGroup() = new DefaultActionGroup(null, true)

}


