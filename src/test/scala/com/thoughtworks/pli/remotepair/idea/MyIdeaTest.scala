package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.testFramework.LightPlatformCodeInsightTestCase

/*
When run the test within idea, add following command line (sample here):

-ea
-Xbootclasspath/p:/Users/twer/workspace/idea-remote-pair/out/test/idea-remote-pair;/Users/twer/workspace/idea-remote-pair/out/production/idea-remote-pair
-XX:+HeapDumpOnOutOfMemoryError
-Xmx512m
-XX:MaxPermSize=320m
-Didea.system.path=/Users/twer/workspace/intellij-community/test-system
-Didea.home.path=/Users/twer/workspace/intellij-community
-Didea.config.path=/Users/twer/workspace/intellij-community/test-config
-Didea.test.group=ALL_EXCLUDE_DEFINED
-Didea.platform.prefix=Idea
 */
class MyIdeaTest extends LightPlatformCodeInsightTestCase {

  import LightPlatformCodeInsightTestCase._

  def testInsertStringAtCaretNotMovingCaret(): Unit = {
    val fileName: String = getTestName(false) + ".txt"
    System.out.println(fileName)
    configureFromFileText(fileName, "text <caret>")
    EditorModificationUtil.insertStringAtCaret(myEditor, "xx", false, false)
    checkResultByText("text <caret>xx")
  }

}
