package com.thoughtworks.pli.intellij.remotepair.actions.dialogs

import com.intellij.openapi.ui.DialogWrapper
import javax.swing.JComponent
import com.intellij.openapi.project.Project
import com.thoughtworks.pli.intellij.remotepair.settings.{ObjectsHolder, AppSettingsProperties}

class ConnectServerDialogWrapper(val project: Project) extends DialogWrapper(project)
with ObjectsHolder
with AppSettingsProperties {

  init()

  override def createCenterPanel(): JComponent = {
    val form = new ConnectServerForm
    form.init("", AppSettingsProperties.DefaultPort, appProperties.clientName)
    form.getMain
  }

  override def doOKAction(): Unit = {
    //    val component = project.getComponent(classOf[RemotePairProjectComponent])
    //    invokeLater {
    //      component.subscribe(ip, port)
    //      component.connect(ip, Integer.parseInt(port), targetProject, userName)
    //      Messages.showMessageDialog(project,
    //        s"Connected to $ip:$port", "Information",
    //        Messages.getInformationIcon)
    //    }
  }
}
