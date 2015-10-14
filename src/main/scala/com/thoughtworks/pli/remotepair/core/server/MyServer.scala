package com.thoughtworks.pli.remotepair.core.server

import com.thoughtworks.pli.intellij.remotepair.ServerLogger
//import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.{ProjectScopeValue, PluginLogger, MySystem}
import com.thoughtworks.pli.remotepair.core.models.{DataKey, MyIde, MyProject}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class MyServer(currentProject: MyProject, myIde: MyIde, mySystem: MySystem, logger: PluginLogger, myClient: MyClient) {
//  val serverHolder = new ProjectScopeValue(currentProject, new DataKey[Option[Server]]("ServerHolderKey"), None)

  def start(port: Int) = myIde.invokeLater {
//    ServerLogger.info = message => logger.info("<server> " + message)
//    val server = new Server(host = None, port)
//    server.start().addListener(new GenericFutureListener[ChannelFuture] {
//      override def operationComplete(f: ChannelFuture) {
//        if (f.isSuccess) {
//          serverHolder.set(Some(server))
//          myIde.invokeLater(currentProject.showMessageDialog(s"Server is started at => ${mySystem.localIp}:$port"))
//        } else {
//          serverHolder.set(None)
//          myIde.invokeLater(currentProject.showErrorDialog("Error", s"Server can't started on $port"))
//        }
//      }
//    })
  }

  def isStarted: Boolean = false // serverHolder.get.isDefined
}
