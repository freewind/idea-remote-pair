package com.thoughtworks.pli.remotepair.idea.dialogs.list

class InitListItems {
  def apply(ignoredList: JList, items: Seq[String]): Unit = {
    val listModel = new DefaultListModel()
    items.foreach(listModel.addElement)
    ignoredList.setModel(listModel)
  }
}
