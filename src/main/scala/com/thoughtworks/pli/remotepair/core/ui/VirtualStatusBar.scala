package com.thoughtworks.pli.remotepair.core.ui

import com.thoughtworks.pli.intellij.remotepair.protocol.{CaretSharingModeRequest, ParallelModeRequest}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject, MyProjectStorage}
import com.thoughtworks.pli.remotepair.core.server.MyServer
import com.thoughtworks.pli.remotepair.core.{DefaultValues, MySystem}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

trait StatusMenuItem {
  def label: String
  def action(): Unit
}

trait VirtualStatusBar {
  def currentProject: MyProject
  def myClient: MyClient
  def myIde: MyIde
  def mySystem: MySystem
  def myProjectStorage: MyProjectStorage
  def dialogFactories: DialogFactories
  def myServer: MyServer

  protected var currentStatus: PairStatus = NotConnect

  val projectInfoItem = new StatusMenuItem {
    override def label: String = {
      val names = myClient.allClients.map(_.name)
      s"Members (${names.mkString(",")})"
    }
    override def action(): Unit = ()
  }

  val copyProjectUrlItem = new StatusMenuItem {
    override def label: String = "Copy project url to clipboard"
    override def action(): Unit = {
      myProjectStorage.projectUrl match {
        case Some(url) => mySystem.copyToClipboard(url)
        case _ =>
      }
    }
  }

  val disconnectItem = new StatusMenuItem {
    def label = "Disconnect"
    def action() = myClient.closeConnection()
  }

  val syncFilesItem = new StatusMenuItem {
    override def label: String = "Sync files"
    override def action(): Unit = {
      val dialog = if (myClient.amIMaster) {
        dialogFactories.createSyncFilesForMasterDialog
      } else {
        dialogFactories.createSyncFilesForSlaveDialog
      }
      dialog.showOnCenter()
    }
  }

  val watchFilesItem = new StatusMenuItem {
    override def label: String = "Show watching files"
    override def action(): Unit = dialogFactories.createWatchFilesDialog(None).showOnCenter()
  }

  val readOnlyItem = new StatusMenuItem {
    override def label: String = (if (myClient.isReadonlyMode) "√ " else "") + "readonly"
    override def action(): Unit = {
      myClient.setReadonlyMode(readonly = !myClient.isReadonlyMode)
    }
  }

  val followCaretModeItem = new StatusMenuItem {
    override def label: String = (if (myClient.isCaretSharing) "√ " else "") + CaretSharingMode.icon
    override def action(): Unit = if (!myClient.isCaretSharing) myClient.publishEvent(CaretSharingModeRequest)
  }

  val parallelModeItem = new StatusMenuItem {
    override def label: String = (if (myClient.isCaretSharing) "" else "√ ") + ParallelMode.icon
    override def action(): Unit = if (myClient.isCaretSharing) myClient.publishEvent(ParallelModeRequest)
  }

  val connectToServerItem = new StatusMenuItem {
    override def label: String = "Connect to server"
    override def action(): Unit = dialogFactories.createConnectServerDialog.showOnCenter()
  }

  val startServerItem = new StatusMenuItem {
    override def label: String = "Start local server"
    override def action(): Unit = myServer.start(DefaultValues.DefaultServerPort)
  }

  val serverInfoItem = new StatusMenuItem {
    override def label: String = myServer.serverHolder.get
      .map(server => s"Local server => ${server.host.getOrElse(mySystem.localIp)}:${server.port}")
      .getOrElse("No local server")
    override def action(): Unit = ()
  }

  val stopServerItem = new StatusMenuItem {
    override def label: String = "stop server"
    override def action(): Unit = myServer.serverHolder.get.foreach { server =>
      myIde.invokeLater {
        server.close().addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture): Unit = {
            if (f.isSuccess) {
              myServer.serverHolder.set(None)
            } else {
              myIde.invokeLater(currentProject.showErrorDialog("Error", "Can't stop server"))
            }
          }
        })
      }
    }
  }

  def statusSummaryText: String = "pair" + serverMessage() + masterMessage() + readonlyMessage() + ": " + currentStatus.icon
  private def serverMessage() = if (myServer.isStarted) " (server)" else ""
  private def masterMessage() = if (myClient.amIMaster) " (master)" else ""
  private def readonlyMessage() = if (myClient.isReadonlyMode) " (readonly)" else ""

  def projectStatusChanged(): Unit = {
    currentStatus = (myClient.isConnected, myClient.isCaretSharing) match {
      case (true, true) => CaretSharingMode
      case (true, false) => ParallelMode
      case _ => NotConnect
    }
  }

}

sealed abstract class PairStatus(val icon: String, val tip: String)
case object NotConnect extends PairStatus("not connect", "not connect yet")
case object CaretSharingMode extends PairStatus("follow carets", "follow others caret changes")
case object ParallelMode extends PairStatus("parallel", "don't follow others caret changes")
