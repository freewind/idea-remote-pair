package com.thoughtworks.pli.remotepair.idea.file

import com.intellij.openapi.editor.Editor

class GetDocumentLength(getDocumentContent: GetDocumentContent) {
  def apply(editor: Editor) = getDocumentContent(editor).length
}
