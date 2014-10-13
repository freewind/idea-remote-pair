package com.thoughtworks.pli.intellij.remotepair.settings

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import com.intellij.ide.util.PropertiesComponent
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo
import com.thoughtworks.pli.intellij.remotepair.CurrentProjectHolder
import com.intellij.openapi.project.Project

class ProjectSettingsPropertiesSpec extends Specification with Mockito {

  "Server host" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.targetServerHost"
    "be stored for specified project" in new Mocking {
      properties.projectProperties.targetServerHost = "another.host"
      there was one(mockService).setValue(key, "another.host")
    }
    "be got if it has been stored for specified project" in new Mocking {
      mockService.getValue(key) returns "stored.host"
      val host = properties.projectProperties.targetServerHost
      host === "stored.host"
    }
    "be empty string if no stored value found" in new Mocking {
      val host = properties.projectProperties.targetServerHost
      host === ""
    }
  }

  "Target Server port" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.targetServerPort"
    "be stored for specified project" in new Mocking {
      properties.projectProperties.targetServerPort = 111
      there was one(mockService).setValue(key, "111")
    }
    "be got if it has been stored for specified project" in new Mocking {
      mockService.getValue(key) returns "111"
      val port = properties.projectProperties.targetServerPort
      port === 111
    }
    "be 8888 if no stored value found " in new Mocking {
      val port = properties.projectProperties.targetServerPort
      port === 8888
    }
  }

  "Client name" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.clientName"
    "be stored for specified project" in new Mocking {
      properties.projectProperties.clientName = "Freewind"
      there was one(mockService).setValue(key, "Freewind")
    }
    "be got if it has been stored for specified project" in new Mocking {
      mockService.getValue(key) returns "Freewind"
      val port = properties.projectProperties.clientName
      port === "Freewind"
    }
    "use the one from application level if no stored value" in new Mocking {
      properties.appProperties.defaultClientName returns "Freewind"
      val port = properties.projectProperties.clientName
      port === "Freewind"
    }
  }

  "Target project" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.targetProject"
    "be stored for specified project" in new Mocking {
      properties.projectProperties.targetProject = "test"
      there was one(mockService).setValue(key, "test")
    }
    "be got if it has been stored for specified project" in new Mocking {
      mockService.getValue(key) returns "test"
      val project = properties.projectProperties.targetProject
      project === "test"
    }
    "use project name as default value if no stored value" in new Mocking {
      mockProject.getName returns "test"
      val project = properties.projectProperties.targetProject
      project === "test"
    }
  }

  "Ignored files" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.ignoredFiles"
    "be stored for specified project" in new Mocking {
      properties.projectProperties.ignoredFiles = Seq("aaa", "bbb")
      there was one(mockService).setValues(key, Array("aaa", "bbb"))
    }
    "be got if it has been stored for specified project" in new Mocking {
      mockService.getValues(key) returns Array("aaa", "bbb")
      val files = properties.projectProperties.ignoredFiles
      files === Seq("aaa", "bbb")
    }
    "use the one from application level if no stored value" in new Mocking {
      properties.appProperties.defaultIgnoredFilesTemplate returns Seq("aaa", "bbb")
      val files = properties.projectProperties.ignoredFiles
      files === Seq("aaa", "bbb")
    }
  }

  trait Mocking extends Scope {
    val mockService = mock[PropertiesComponent]
    val mockProject = mock[Project]
    val properties = new ProjectSettingsProperties with CurrentProjectHolder with IdeaPluginServices with AppSettingsProperties with LocalHostInfo {
      override val currentProject: Project = mockProject

      override def projectPropertiesService(project: Project) = mockService

      override val appProperties = mock[AppProperties]
    }

  }

}
