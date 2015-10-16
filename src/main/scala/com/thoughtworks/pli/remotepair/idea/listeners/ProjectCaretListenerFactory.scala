package com.thoughtworks.pli.remotepair.idea.listeners

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.event.{CaretAdapter, CaretEvent, CaretListener}
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.remotepair.core._
import com.thoughtworks.pli.remotepair.core.editor_event_handlers.{EditorCaretChangeEvent, HandleIdeaEvent}
import com.thoughtworks.pli.remotepair.core.models.IdeaFactories

class ProjectCaretListenerFactory(logger: PluginLogger, handleIdeaEvent: HandleIdeaEvent, ideaFactories: IdeaFactories)
  extends ListenerManager[CaretListener] {

  val key = new Key[CaretListener]("remote_pair.listeners.caret")

  def createNewListener(editor: Editor, file: VirtualFile, project: Project): CaretListener = new CaretAdapter {

    override def caretPositionChanged(e: CaretEvent): Unit = {
      logger.info("caretPositionChanged event: " + info(e))
      handleIdeaEvent(new EditorCaretChangeEvent(ideaFactories(file), ideaFactories(editor), e.getCaret.getOffset))
    }

    private def info(e: CaretEvent) = s"${e.getOldPosition} => ${e.getNewPosition}"
  }

  override def originRemoveListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.removeCaretListener

  override def originAddListener(editor: Editor): (CaretListener) => Any = editor.getCaretModel.addCaretListener

}

