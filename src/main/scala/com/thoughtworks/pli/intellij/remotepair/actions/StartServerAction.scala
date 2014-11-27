package com.thoughtworks.pli.intellij.remotepair.actions

import com.intellij.openapi.actionSystem.{AnAction, AnActionEvent}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.intellij.remotepair.settings.{AppSettingsProperties, IdeaPluginServices}
import com.thoughtworks.pli.intellij.remotepair.{InvokeLater, Projects, RichProject}
import io.netty.channel.ChannelFuture
import io.netty.util.concurrent.GenericFutureListener

class StartServerAction extends AnAction("start") with AppSettingsProperties with IdeaPluginServices {
  def actionPerformed(event: AnActionEvent) {
    val project = event.getProject
    val port = appProperties.serverBindingPort
    new ServerStarter(Projects.init(project)).start(port)
  }

  class ServerStarter(override val currentProject: RichProject) extends CurrentProjectHolder with InvokeLater with LocalHostInfo {
    def start(port: Int) = invokeLater {
      val server = new Server
      server.start(port).addListener(new GenericFutureListener[ChannelFuture] {
        override def operationComplete(f: ChannelFuture) {
          if (f.isSuccess) {
            currentProject.server = Some(server)
            invokeLater(currentProject.showMessageDialog(s"Server is started at: ${localIp()}:$port"))
          } else {
            currentProject.server = None
            invokeLater(currentProject.showErrorDialog("Error", s"Server can't started on $port"))
          }
        }
      })
    }
  }

}


