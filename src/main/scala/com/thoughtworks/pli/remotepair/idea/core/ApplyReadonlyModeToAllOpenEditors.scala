package com.thoughtworks.pli.remotepair.idea.core

import com.thoughtworks.pli.remotepair.idea.utils.RunReadAction

class ApplyReadonlyModeToAllOpenEditors(getAllTextEditors: GetAllTextEditors, applyEditorReadonlyMode: ApplyEditorReadonlyMode, runReadAction: RunReadAction) {
  def apply(): Unit = {
    runReadAction {
      getAllTextEditors().foreach(applyEditorReadonlyMode.apply)
    }
  }
}
