package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.intellij.openapi.diagnostic.Logger
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair._
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.idea.Module
import com.thoughtworks.pli.remotepair.idea.core.RichProjectFactory.RichProject
import com.thoughtworks.pli.remotepair.idea.settings.ServerPortInStorage
import com.thoughtworks.pli.remotepair.idea.utils.{GetLocalIp, InvokeLater}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StartServerAction extends AnAction("Start local server") {

  def actionPerformed(event: AnActionEvent) {
    new Module {
      override def rawProject: Project = event.getProject
    }.startServer()
  }

}

case class StartServer(currentProject: RichProject, invokeLater: InvokeLater, localIp: GetLocalIp, serverPortInStorage: ServerPortInStorage, logger: Logger) {
  def apply(port: Int = serverPortInStorage.load()) = invokeLater {
    ServerLogger.info = logger.info
    val server = new Server(host = None, port)
    server.start().addListener(new GenericFutureListener[ChannelFuture] {
      override def operationComplete(f: ChannelFuture) {
        if (f.isSuccess) {
          currentProject.server = Some(server)
          invokeLater(currentProject.showMessageDialog(s"Server is started at => ${localIp()}:$port"))
        } else {
          currentProject.server = None
          invokeLater(currentProject.showErrorDialog("Error", s"Server can't started on $port"))
        }
      }
    })
  }
}


