package com.thoughtworks.pli.intellij.remotepair.client

trait InitializingProcessCreator {
  def createInitializingProcess(): InitializingProcess = ClientObjects.initializingProcess
}
