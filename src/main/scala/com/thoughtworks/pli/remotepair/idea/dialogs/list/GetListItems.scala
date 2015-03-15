package com.thoughtworks.pli.remotepair.idea.dialogs.list

class GetListItems {
  def apply(ignoredList: JList): Seq[String] = {
    val model = ignoredList.getModel
    (0 until model.getSize).map(model.getElementAt).map(_.asInstanceOf[String]).toList
  }
}
