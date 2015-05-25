package com.thoughtworks.pli.remotepair.in_memory_ide

import com.thoughtworks.pli.remotepair.core.models.MyFile
import com.thoughtworks.pli.remotepair.core.ui.VirtualComponents._

object MemoryUiComponents {

  class MemoryComponent {
    var visible = true
    private var _requestedFocus = false
    def requestFocus(): Unit = {
      _requestedFocus = true
    }
    def hasRequestedFocus: Boolean = this._requestedFocus
  }

  class MemoryInputField extends MemoryComponent with VirtualInputField {
    var text: String = ""
  }

  class MemoryCheckbox extends MemoryComponent with VirtualCheckBox {
    var isSelected = false
  }

  class MemoryLabel extends MemoryComponent with VirtualLabel {
    var text: String = ""
  }

  class MemoryProgressBar extends MemoryComponent with VirtualProgressBar {
    private var _value = 0
    private var _max = 0
    def value: Int = _value
    def value_=(value: Int) = _value = value
    def max: Int = _max
    def max_=(value: Int) = _max = value
  }

  class MemoryButton extends MemoryComponent with VirtualButton {
    var text = ""
    var enabled = true
    private var _onClick: () => Unit = _
    def onClick(f: => Unit): Unit = {
      _onClick = () => f
    }
  }

  class MemoryDialog extends MemoryComponent with VirtualDialog {
    var title = ""
    private var _onOpen: () => Unit = _
    private var _onClose: () => Unit = _
    private var _disposed: Boolean = false

    def onOpen(f: => Unit) = _onOpen = () => f
    def onClose(f: => Unit) = _onClose = () => f
    def dispose(): Unit = _disposed = true
    def disposed: Boolean = _disposed
  }

  class MemoryList extends MemoryComponent with VirtualList {
    var items: Seq[String] = Nil
    var selectedItems: Seq[String] = Nil
    def removeItems(values: Seq[String]): Unit = items = items.filterNot(values.contains)
  }

  class MemoryFileTree extends MemoryComponent with VirtualFileTree {
    var _file: MyFile = _
    var _filter: MyFile => Boolean = _
    // FIXME
    def init(file: MyFile, filter: MyFile => Boolean) = {
      _file = file
      _filter = filter
    }
    var selectedFiles: Seq[String] = Nil
  }

}
