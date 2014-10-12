package com.thoughtworks.pli.intellij.remotepair.settings

import org.specs2.mutable.Specification
import org.specs2.specification.Scope
import com.intellij.ide.util.PropertiesComponent
import org.specs2.mock.Mockito
import com.thoughtworks.pli.intellij.remotepair.actions.LocalHostInfo

class AppSettingsPropertiesSpec extends Specification with Mockito {

  "Server port" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.serverBindingPort"
    "be stored" in new Mocking {
      properties.appProperties.serverBindingPort = 111
      there was one(mockService).setValue(key, "111")
    }
    "be got" in new Mocking {
      mockService.getValue(key) returns "111"
      val port = properties.appProperties.serverBindingPort
      port === 111
    }
    "use 8888 as the default port" in new Mocking {
      val port = properties.appProperties.serverBindingPort
      port === 8888
    }
  }

  "Client name" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.clientName"
    "be stored" in new Mocking {
      properties.appProperties.clientName = "Freewind"
      there was one(mockService).setValue(key, "Freewind")
    }
    "be got" in new Mocking {
      mockService.getValue(key) returns "Freewind"
      val name = properties.appProperties.clientName
      name === "Freewind"
    }
    "use local host name as default value" in new Mocking {
      val name = properties.appProperties.clientName
      name === "MyComputer"
    }
  }

  "Default ignored files" should {
    val key = "com.thoughtworks.pli.intellij.remotepair.defaultIgnoredFiles"
    "be stored" in new Mocking {
      properties.appProperties.defaultIgnoredFilesTemplate = Seq("aaa", "bbb")
      there was one(mockService).setValues(key, Array("aaa", "bbb"))
    }
    "be got" in new Mocking {
      mockService.getValues(key) returns Array("aaa", "bbb")
      val files = properties.appProperties.defaultIgnoredFilesTemplate
      files === Seq("aaa", "bbb")
    }
    "use empty array as default value" in new Mocking {
      val files = properties.appProperties.defaultIgnoredFilesTemplate
      files === Seq.empty[String]
    }
  }

  trait Mocking extends Scope {
    val mockService = mock[PropertiesComponent]
    val properties = new AppSettingsProperties with IdeaPluginServices with LocalHostInfo {
      override val appPropertiesService = mockService

      override def localHostName() = "MyComputer"
    }
  }

}
