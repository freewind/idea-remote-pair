package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.server.ClientIdName
import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyFile, MyProject}
import com.thoughtworks.pli.remotepair.core.{MyUtils, PluginLogger, ProjectScopeValue}
import com.thoughtworks.pli.remotepair.idea.dialogs.{CreateFileTree, FileTreeNode}
import io.netty.channel.{ChannelFuture, ChannelHandlerContext}
import io.netty.util.concurrent.GenericFutureListener

import scala.concurrent.{Future, Promise}

trait MyClientData {
  def currentProject: MyProject
  val serverStatusHolder = new ProjectScopeValue(currentProject, new DataKey[Option[ServerStatusResponse]]("ServerStatusHolderKey"), None)
  val clientInfoHolder = new ProjectScopeValue(currentProject, new DataKey[Option[ClientInfoResponse]]("ClientInfoHolderKey"), None)
  protected val connectionHolder = new ProjectScopeValue(currentProject, new DataKey[Option[ChannelHandlerContext]]("ConnectionHolderKey"), None)
  val channelHandlerHolder = new ProjectScopeValue(currentProject, new DataKey[Option[MyChannelHandler]]("ChannelHandlerHolderKey"), None)
  private val readonlyModeHolder = new ProjectScopeValue(currentProject, new DataKey[Boolean]("ReadonlyModeHolderKey"), false)

  def setConnection(value: Option[ChannelHandlerContext]): Unit = this.connectionHolder.set(value)
  def isReadonlyMode: Boolean = readonlyModeHolder.get
  def setReadonlyMode(readonly: Boolean): Unit = readonlyModeHolder.set(readonly)
}

class MyClient(val currentProject: MyProject, myUtils: MyUtils, createFileTree: CreateFileTree, logger: => PluginLogger) extends MyClientData {
  def amIMaster: Boolean = clientInfoHolder.get.exists(_.isMaster)
  def myClientId: Option[String] = clientInfoHolder.get.map(_.clientId)
  def myClientName: Option[String] = clientInfoHolder.get.map(_.name)
  def idName: ClientIdName = ClientIdName(myClientId.get, myClientName.get)
  def masterClient: Option[ClientInfoResponse] = projectInfoData.flatMap(_.clients.find(_.isMaster))
  def isCaretSharing: Boolean = projectInfoData.exists(_.isCaretSharing)
  def allClients: Seq[ClientInfoResponse] = projectInfoData.toSeq.flatMap(_.clients)
  def otherClients: Seq[ClientInfoResponse] = allClients.filterNot(client => Some(client.clientId) == myClientId)
  def masterClientId: Option[String] = projectInfoData.flatMap(_.clients.find(_.isMaster)).map(_.clientId)
  def serverWatchingFiles: Seq[String] = projectInfoData.map(_.watchingFiles).getOrElse(Nil)
  def closeConnection(): Unit = connectionHolder.get.foreach(_.close())
  def clientIdToName(clientId: String): Option[String] = {
    projectInfoData.flatMap(_.clients.find(_.clientId == clientId)).map(_.name)
  }

  def isWatching(file: MyFile): Boolean = file.relativePath.exists(path => serverWatchingFiles.exists(myUtils.isSubPath(path, _)))
  def watchingFileSummaries: Seq[FileSummary] = allWatchingFiles.flatMap(_.summary)
  def allWatchingFiles: Seq[MyFile] = {
    val tree = createFileTree(currentProject.baseDir, isWatching)
    toList(tree).filterNot(_.isDirectory).filterNot(_.isBinary)
  }
  def isConnected: Boolean = connectionHolder.get.isDefined

  def publishEvent(event: PairEvent): Future[Unit] = {
    connectionHolder.get match {
      case Some(conn) => {
        val p = Promise[Unit]()
        logger.info(s"publish to server: ${event.toMessage}")
        conn.writeAndFlush(event.toMessage).addListener(new GenericFutureListener[ChannelFuture] {
          override def operationComplete(f: ChannelFuture): Unit = {
            if (f.cause() != null) {
              p.failure(f.cause())
            } else {
              p.success(())
            }
          }
        })
        p.future
      }
      case _ => Future.failed(new IllegalStateException("No server connection available"))
    }
  }

  private def toList(tree: FileTreeNode): List[MyFile] = {
    def fetchChildren(node: FileTreeNode, result: List[MyFile]): List[MyFile] = {
      if (node.getChildCount == 0) result
      else {
        val nodes = (0 until node.getChildCount).map(node.getChildAt).map(_.asInstanceOf[FileTreeNode]).toList
        nodes.foldLeft(nodes.map(_.data.file) ::: result) {
          case (all, child) => fetchChildren(child, all)
        }
      }
    }
    fetchChildren(tree, Nil)
  }

  def projectInfoData: Option[ProjectInfoData] = for {
    server <- serverStatusHolder.get
    client <- clientInfoHolder.get
    p <- server.projects.find(_.name == client.project)
  } yield p
}
