//package com.thoughtworks.pli.remotepair.core.server_event_handlers
//
//import com.thoughtworks.pli.remotepair.core.models.MyFile
//import com.thoughtworks.pli.intellij.remotepair.protocol.OpenTabEvent
//import com.thoughtworks.pli.remotepair.idea.MocksModule
//import com.thoughtworks.pli.remotepair.core.TabEventLock
//import com.thoughtworks.pli.remotepair.core.server_event_handlers.editors.HandleOpenTabEvent
//import org.specs2.mock.Mockito
//import org.specs2.mutable.Specification
//
//class HandleOpenTabEventSpec extends Specification with Mockito with MocksModule {
//  isolated
//
//  override lazy val handleOpenTabEvent = new HandleOpenTabEvent(getFileByRelative, openFileInTab, tabEventsLocksInProject, getCurrentTimeMillis, isFileInActiveTab)
//
//  val file = mock[MyFile]
//  val event = new OpenTabEvent("/abc")
//  getFileByRelative.apply("/abc") returns Some(file)
//  getCurrentTimeMillis.apply() returns 123456L
//  tabEventsLocksInProject.isEmpty returns false
//  isFileInActiveTab.apply(file) returns false
//
//  "When client receives OpenTabEvent, it" should {
//    "open corresponding tab and hold the path & currentTime in project for later use" in {
//      handleOpenTabEvent(event)
//      there was one(openFileInTab).apply(file)
//      there was one(tabEventsLocksInProject).lock(TabEventLock("/abc", 123456L))
//    }
//    "not do anything if the asked file is opened and there is no lock existed" in {
//      isFileInActiveTab.apply(file) returns true
//      tabEventsLocksInProject.isEmpty returns true
//      handleOpenTabEvent(event)
//      there was no(openFileInTab).apply(any[MyFile])
//      there was no(tabEventsLocksInProject).lock(any)
//    }
//  }
//
//}
