import java.io.{File, StringReader}
import java.util.{List => JList}

import org.jdom.input.SAXBuilder
import org.jdom.output.XMLOutputter
import org.jdom.{Document, Element}
import sbt.IO

import scala.collection.JavaConversions._
import scala.util.{Failure, Success, Try}

object PluginConverter {

  def convertToPlugin(file: File): Unit = {
    withDocument(file) { document: Document =>
      val rootNode = document.getRootElement
      if (getAttr(rootNode, "type") == Some("PLUGIN_MODULE")) {
        Success(false)
      } else {
        rootNode.setAttribute("type", "PLUGIN_MODULE")
        addChild(rootNode, "component", "name" -> "DevKit.ModuleBuildProperties", "url" -> "file://$MODULE_DIR$/../../META-INF/plugin.xml")
        Success(true)
      }
    }
  }

  def createPluginTask(file: File, moduleName: String): Unit = {
    val taskName = moduleName
    withDocument(file) { document: Document =>
      getChildren(document.getRootElement, Some("component")).find(getAttr(_, "name") == Some("RunManager")) match {
        case Some(runManager) => getChildren(runManager, Some("configuration")).find(conf => {
          getAttr(conf, "name") == Some(taskName) && getAttr(conf, "factoryName") == Some("Plugin")
        }) match {
          case Some(conf) => Success(false)
          case _ =>
            runManager.setAttribute("selected", s"Plugin.$taskName")
            val list = addChild(runManager, "list", "size" -> "1")
            addChild(list, "item", "index" -> "0", "class" -> "java.lang.String", "itemvalue" -> s"Plugin.$taskName")
            val conf = addChild(runManager, "configuration",
              "default" -> "false",
              "name" -> taskName,
              "type" -> "#org.jetbrains.idea.devkit.run.PluginConfigurationType",
              "factoryName" -> "Plugin")
            addChild(conf, "module", "name" -> moduleName)
            addChild(conf, "option", "name" -> "VM_PARAMETERS", "value" -> "-Xmx512m -Xms256m -XX:MaxPermSize=250m -ea")
            addChild(conf, "option", "name" -> "PROGRAM_PARAMETERS", "value" -> "")
            addChild(conf, "method")
            Success(true)
        }
        case _ => Failure(new Exception("can't find the RunManager component"))
      }
    }
  }

  private def getChildren(element: Element, name: Option[String] = None): JList[Element] = {
    name match {
      case Some(n) => element.getChildren(n).asInstanceOf[JList[Element]]
      case _ => element.getChildren.asInstanceOf[JList[Element]]
    }
  }

  private def getAttr(element: Element, name: String): Option[String] = {
    val attr = element.getAttribute(name)
    Option(attr).map(_.getValue)
  }

  private def addChild(parent: Element, name: String, attrs: (String, String)*): Element = {
    val child = new Element(name)
    attrs.foreach({ case (key, value) => child.setAttribute(key, value)})
    getChildren(parent).add(child)
    child
  }

  private def withDocument(file: File)(transform: Document => Try[Boolean]): Unit = {
    if (file.exists()) {
      val content = IO.read(file)
      val builder = new SAXBuilder()
      val document = builder.build(new StringReader(content))
      transform(document) match {
        case Success(true) => IO.write(file, new XMLOutputter().outputString(document))
          println("Success! Please restart your IDEA to apply the change!")
        case Success(_) => println("Already applied, skip")
        case Failure(e) => println("Error: " + e.getMessage)
      }
    } else {
      println("Error: file is not found: " + file)
    }
  }

}
