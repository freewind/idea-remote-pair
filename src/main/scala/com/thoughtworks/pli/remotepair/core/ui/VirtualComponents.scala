package com.thoughtworks.pli.remotepair.core.ui

import com.thoughtworks.pli.remotepair.core.models.MyFile

object VirtualComponents {

  trait VirtualComponent {
    def requestFocus(): Unit
  }

  trait VirtualInputField extends VirtualComponent {
    def text: String
    def text_=(value: String): Unit
  }

  trait VirtualButton extends VirtualComponent {
    def text_=(value: String): Unit
    def onClick(f: => Any): Unit
    def enabled_=(value: Boolean): Unit
    def enabled: Boolean
  }

  trait VirtualDialog extends VirtualComponent {
    def title: String
    def title_=(title: String): Unit
    def onOpen(f: => Any): Unit
    def onClose(f: => Any): Unit
    def dispose(): Unit
  }

  trait VirtualLabel extends VirtualComponent {
    def text: String
    def text_=(value: String): Unit
    def visible_=(value: Boolean): Unit
    def visible: Boolean
  }

  trait VirtualCheckBox extends VirtualComponent {
    def isSelected: Boolean
  }

  trait VirtualProgressBar extends VirtualComponent {
    def max: Int
    def max_=(value: Int): Unit
    def value: Int
    def value_=(value: Int): Unit
  }

  trait VirtualList extends VirtualComponent {
    def items: Seq[String]
    def items_=(value: Seq[String]): Unit
    def selectedItems: Seq[String]
    def removeItems(value: Seq[String]): Unit
  }

  trait VirtualFileTree extends VirtualComponent {
    def init(file: MyFile, fileToBoolean: (MyFile) => Boolean): Unit
    def selectedFiles: Seq[String]
  }

}
