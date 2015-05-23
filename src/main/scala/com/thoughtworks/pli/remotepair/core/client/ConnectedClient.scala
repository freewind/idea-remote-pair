package com.thoughtworks.pli.remotepair.core.client

import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.intellij.remotepair.utils.IsSubPath
import com.thoughtworks.pli.remotepair.core.ProjectScopeValue
import com.thoughtworks.pli.remotepair.core.models.{MyFile, MyProject}
import com.thoughtworks.pli.remotepair.core.tree.{CreateFileTree, FileTreeNode}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

import scala.concurrent.{Future, Promise}

class ConnectedClient(currentProject: MyProject, isSubPath: IsSubPath, createFileTree: CreateFileTree) {

  val serverHolder = new ProjectScopeValue[Option[Server]](currentProject, "ServerHolderKey", None)
  val serverStatusHolder = new ProjectScopeValue[Option[ServerStatusResponse]](currentProject, "ServerStatusHolderKey", None)
  val clientInfoHolder = new ProjectScopeValue[Option[ClientInfoResponse]](currentProject, "ClientInfoHolderKey", None)
  val connectionHolder = new ProjectScopeValue[Option[Connection]](currentProject, "ConnectionHolderKey", None)
  val channelHandlerHolder = new ProjectScopeValue[Option[MyChannelHandler]](currentProject, "ChannelHandlerHolderKey", None)
  val readonlyModeHolder = new ProjectScopeValue[Option[Boolean]](currentProject, "ReadonlyModeHolderKey", None)

  def amIMaster: Boolean = clientInfoHolder.get.exists(_.isMaster)
  def myClientId: Option[String] = clientInfoHolder.get.map(_.clientId)
  def myClientName: Option[String] = clientInfoHolder.get.map(_.name)
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
  def isReadonlyMode: Boolean = readonlyModeHolder.get.getOrElse(false)
  def setReadonlyMode(readonly: Boolean): Unit = readonlyModeHolder.set(Some(readonly))

  def isWatching(file: MyFile): Boolean = file.relativePath.exists(path => serverWatchingFiles.exists(isSubPath(path, _)))
  def watchingFileSummaries: Seq[FileSummary] = allWatchingFiles.flatMap(_.summary)
  def allWatchingFiles: Seq[MyFile] = {
    val tree = createFileTree(currentProject.baseDir, isWatching)
    toList(tree).filterNot(_.isDirectory).filterNot(_.isBinary)
  }

  def publishEvent(event: PairEvent): Future[Unit] = {
    connectionHolder.get match {
      case Some(conn) => {
        val p = Promise[Unit]()
        conn.publish(event).addListener(new GenericFutureListener[ChannelFuture] {
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
