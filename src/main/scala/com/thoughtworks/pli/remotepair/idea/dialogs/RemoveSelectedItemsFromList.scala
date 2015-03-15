package com.thoughtworks.pli.remotepair.idea.dialogs

import javax.swing.{DefaultListModel, JList}

class RemoveSelectedItemsFromList {
  def apply(ignoredList: JList): Unit = {
    val listModel = ignoredList.getModel.asInstanceOf[DefaultListModel]
    ignoredList.getSelectedValues.foreach(listModel.removeElement)
  }
}
