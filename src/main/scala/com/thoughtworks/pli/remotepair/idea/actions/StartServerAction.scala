package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.core.models.{MyPlatform, MyProject}
import com.thoughtworks.pli.remotepair.core.{PluginLogger, ServerHolder}
import com.thoughtworks.pli.remotepair.idea.Module
import com.thoughtworks.pli.remotepair.idea.idea.{ShowErrorDialog, ShowMessageDialog}
import com.thoughtworks.pli.remotepair.idea.settings.ServerPortInGlobalStorage
import com.thoughtworks.pli.remotepair.idea.utils.GetLocalIp
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StartServerAction extends AnAction("Start local server") {

  def actionPerformed(event: AnActionEvent) {
    new Module {
      override def currentIdeaProject: Project = event.getProject
    }.startServer()
  }

}

case class StartServer(currentProject: MyProject, myPlatform: MyPlatform, getLocalIp: GetLocalIp, serverPortInGlobalStorage: ServerPortInGlobalStorage, logger: PluginLogger, serverHolder: ServerHolder, showMessageDialog: ShowMessageDialog, showErrorDialog: ShowErrorDialog) {
  def apply(port: Int = serverPortInGlobalStorage.load()) = myPlatform.invokeLater {
    ServerLogger.info = message => logger.info("<server> " + message)
    val server = new Server(host = None, port)
    server.start().addListener(new GenericFutureListener[ChannelFuture] {
      override def operationComplete(f: ChannelFuture) {
        if (f.isSuccess) {
          serverHolder.put(Some(server))
          myPlatform.invokeLater(showMessageDialog(s"Server is started at => ${getLocalIp()}:$port"))
        } else {
          serverHolder.put(None)
          myPlatform.invokeLater(showErrorDialog("Error", s"Server can't started on $port"))
        }
      }
    })
  }
}


