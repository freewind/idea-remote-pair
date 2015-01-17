package com.thoughtworks.pli.remotepair.idea.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.idea.core._
import com.thoughtworks.pli.remotepair.idea.settings.{AppSettingsProperties, IdeaPluginServices}
import com.thoughtworks.pli.intellij.remotepair._
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StartServerAction extends AnAction("Start local server") with AppSettingsProperties with IdeaPluginServices {
  def actionPerformed(event: AnActionEvent) {
    val project = event.getProject
    val port = appProperties.serverBindingPort
    new ServerStarter(Projects.init(project)).start(port)
  }

  class ServerStarter(override val currentProject: RichProject) extends CurrentProjectHolder with InvokeLater with LocalHostInfo with AppLogger {
    def start(port: Int) = invokeLater {
      ServerLogger.info = log.info
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

}


