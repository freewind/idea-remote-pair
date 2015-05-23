package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.core.{MySystem, PluginLogger}
import com.thoughtworks.pli.remotepair.core.client.MyClient
import com.thoughtworks.pli.remotepair.core.models.{MyIde, MyProject}
import com.thoughtworks.pli.remotepair.idea.{DefaultValues, Module}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StartServerAction extends AnAction("Start local server") {

  def actionPerformed(event: AnActionEvent) {
    new Module {
      override def currentIdeaProject: Project = event.getProject
    }.startServer(DefaultValues.DefaultPort)
  }

}

case class StartServer(currentProject: MyProject, myPlatform: MyIde, mySystem: MySystem, logger: PluginLogger, myClient: MyClient) {
  def apply(port: Int) = myPlatform.invokeLater {
    ServerLogger.info = message => logger.info("<server> " + message)
    val server = new Server(host = None, port)
    server.start().addListener(new GenericFutureListener[ChannelFuture] {
      override def operationComplete(f: ChannelFuture) {
        if (f.isSuccess) {
          myClient.serverHolder.set(Some(server))
          myPlatform.invokeLater(currentProject.showMessageDialog(s"Server is started at => ${mySystem.localIp}:$port"))
        } else {
          myClient.serverHolder.set(None)
          myPlatform.invokeLater(currentProject.showErrorDialog("Error", s"Server can't started on $port"))
        }
      }
    })
  }
}


