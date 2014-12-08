package com.thoughtworks.pli.intellij.remotepair

import java.awt.Color
import java.nio.charset.Charset

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.ScrollType
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup._
import com.intellij.openapi.fileEditor.TextEditor
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{JoinProjectDialog, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client.CurrentProjectHolder
import com.thoughtworks.pli.intellij.remotepair.ui.PairCaretComponent
import com.thoughtworks.pli.intellij.remotepair.utils.Md5Support
import io.netty.bootstrap.Bootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringDecoder, StringEncoder}

trait Subscriber extends AppLogger with PublishEvents with EventHandler with EventParser {
  this: CurrentProjectHolder =>

  class MyChannelHandler extends ChannelHandlerAdapter {

    override def channelActive(ctx: ChannelHandlerContext) {
      currentProject.context = Some(ctx)
    }

    override def channelInactive(ctx: ChannelHandlerContext) {
      currentProject.context = None
      workerGroup.shutdownGracefully()
    }

    override def channelRead(ctx: ChannelHandlerContext, msg: Any) {
      msg match {
        case line: String =>
          println(s"Plugin ${currentProject.clientInfo.map(_.name).getOrElse("Unknown")} receives line: $line")
          handleEvent(parseEvent(line))
        case _ =>
      }
    }

    override def exceptionCaught(ctx: ChannelHandlerContext, cause: Throwable) {
      cause.printStackTrace()
    }
  }

  object MyChannelInitializer extends ChannelInitializer[SocketChannel] {
    override def initChannel(ch: SocketChannel) {
      ch.pipeline().addLast(
        new LineBasedFrameDecoder(Int.MaxValue),
        new StringDecoder(Charset.forName("UTF-8")),
        new StringEncoder(Charset.forName("UTF-8")),
        new MyChannelHandler())
    }
  }

  var workerGroup: NioEventLoopGroup = _
  var bootstrap: Bootstrap = _

  def subscribe(ip: String, port: Int) = {
    workerGroup = new NioEventLoopGroup()
    bootstrap = new Bootstrap()
    bootstrap.group(workerGroup)
    bootstrap.channel(classOf[NioSocketChannel])
    bootstrap.option(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)
    bootstrap.handler(MyChannelInitializer)
    bootstrap.connect(ip, port).sync()
  }

}

trait EventHandler extends TabEventHandler with ChangeContentEventHandler with ResetContentEventHandler with Md5Support with AppLogger with PublishEvents with DialogsCreator with SelectionEventHandler with PublishSyncFilesRequest with CurrentProjectHolder {

  def handleEvent(event: PairEvent) {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event.path)
      case event: CloseTabEvent => handleCloseTabEvent(event.path)
      case event: ChangeContentEvent => handleChangeContentEvent(event)
      case event: ResetContentEvent => handleResetContentEvent(event)
      case event: ResetTabEvent => handleOpenTabEvent(event.path)
      case event: ResetContentRequest => handleResetContentRequest(event)
      case ResetTabRequest => handleResetTabRequest()
      case event: MoveCaretEvent => moveCaret(event.path, event.offset)
      case event: SelectContentEvent => highlightPairSelection(event)
      case event: ServerErrorResponse => showErrorDialog(event)
      case event: ServerStatusResponse => handleServerStatusResponse(event)
      case AskForJoinProject(message) => handleAskForJoinProject(message)
      case event: ClientInfoResponse => handleClientInfoResponse(event)
      case req: SyncFilesRequest => handleSyncFilesRequest(req)
      case SyncFilesForAll => handleSyncFilesForAll()
      case event: MasterPairableFiles => handleMasterPairableFiles(event)
      case event: SyncFileEvent => handleSyncFileEvent(event)
      case event: CreateDirEvent => handleCreateDirEvent(event)
      case event: CreateFileEvent => handleCreateFileEvent(event)
      case event: DeleteFileEvent => handleDeleteFileEvent(event)
      case event: DeleteDirEvent => handleDeleteDirEvent(event)
      case _ => println("!!!! Can't handle: " + event)
    }
  }

  private def handleCreateDirEvent(event: CreateDirEvent): Unit = runWriteAction {
    currentProject.findOrCreateDir(event.path)
  }

  def forceWriteTextFile(relativePath: String, content: Content): Unit = {
    currentProject.getTextEditorsOfPath(relativePath) match {
      case Nil => val file = currentProject.getFileByRelative(relativePath)
        .getOrElse(currentProject.findOrCreateFile(relativePath))
        file.setBinaryContent(content.text.getBytes(content.charset))
      case editors => editors.foreach { editor =>
        editor.getEditor.getDocument.setText(content.text)
        currentProject.getDocumentManager.saveDocument(editor.getEditor.getDocument)
      }
    }
  }

  private def handleCreateFileEvent(event: CreateFileEvent): Unit = runWriteAction {
    forceWriteTextFile(event.path, event.content)
  }

  private def handleDeleteFileEvent(event: DeleteFileEvent): Unit = runWriteAction {
    currentProject.deleteFile(event.path)
  }

  private def handleDeleteDirEvent(event: DeleteDirEvent): Unit = runWriteAction {
    currentProject.deleteDir(event.path)
  }

  private def handleSyncFileEvent(event: SyncFileEvent): Unit = {
    runWriteAction(forceWriteTextFile(event.path, event.content))
  }

  private def handleSyncFilesRequest(req: SyncFilesRequest): Unit = {
    val files = currentProject.getAllPairableFiles(currentProject.projectInfo.map(_.ignoredFiles).getOrElse(Nil))
    val diffs = calcDifferentFiles(files, req.fileSummaries)
    publishEvent(MasterPairableFiles(diffs.map(currentProject.getRelativePath)))
    diffs.foreach(file => publishEvent(SyncFileEvent(currentProject.getRelativePath(file), currentProject.getFileContent(file))))
  }

  private def handleSyncFilesForAll(): Unit = invokeLater {
    publishSyncFilesRequest()
  }

  private def calcDifferentFiles(localFiles: Seq[VirtualFile], fileSummaries: Seq[FileSummary]): Seq[VirtualFile] = {
    def isSameWithRemote(file: VirtualFile) = fileSummaries.contains(currentProject.getFileSummary(file))
    localFiles.filter(isSameWithRemote)
  }

  private def handleMasterPairableFiles(event: MasterPairableFiles): Unit = {
    val ignoredFiles = currentProject.projectInfo.map(_.ignoredFiles).getOrElse(Nil).toList
    invokeLater {
      currentProject.getAllPairableFiles(ignoredFiles).foreach { myFile =>
        if (!event.paths.contains(currentProject.getRelativePath(myFile))) {
          println("#### delete file which is not exist on master side: " + myFile.getPath)
          if (myFile.exists()) {
            runWriteAction(myFile.delete(this))
          }
        }
      }
    }
  }

  private def handleClientInfoResponse(event: ClientInfoResponse) {
    currentProject.clientInfo = Some(event)
  }

  private def handleAskForJoinProject(message: Option[String]) {
    invokeLater(createJoinProjectDialog(message).show())
  }

  private def handleResetContentRequest(event: ResetContentRequest) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      runReadAction {
        val content = editor.getEditor.getDocument.getText
        val eee = new ResetContentEvent(event.path, content, md5(content))
        publishEvent(eee)
      }
    }
  }

  private def handleResetTabRequest() {
    // FIXME it can be no opened tab
    invokeLater {
      val path = currentProject.pathOfSelectedTextEditor.getOrElse("")
      publishEvent(ResetTabEvent(path))
    }
  }

  val pairCaretComponentKey = new Key[PairCaretComponent]("pair-caret-component")
  private def moveCaret(path: String, offset: Int) {
    def caretPosition(editor: EditorEx, offset: Int) = {
      editor.logicalPositionToXY(editor.offsetToLogicalPosition(offset))
    }
    def createPairCaretInEditor(editor: EditorEx, offset: Int) = {
      var component = editor.getUserData[PairCaretComponent](pairCaretComponentKey)
      if (component == null) {
        println(">>>>>>>>>>>>>>>>>>>>>>>>>>>>>>>> add new pairComponent!!!!!!!!!!!!!!!!!!! ")
        component = new PairCaretComponent
        editor.getContentComponent.add(component)
        editor.putUserData(pairCaretComponentKey, component)
      }

      val viewport = editor.getContentComponent.getVisibleRect
      component.setBounds(0, 0, viewport.width, viewport.height)
      val position = caretPosition(editor, offset)
      if (position.x > 0) {
        position.x -= 1
      }

      component.setLocation(position)
      component.lineHeight = editor.getLineHeight
      component
    }

    def scrollToCaret(editor: EditorEx, offset: Int): Unit = {
      val position = caretPosition(editor, offset)
      if (!editor.getContentComponent.getVisibleRect.contains(position)) {
        editor.getScrollingModel.scrollTo(editor.xyToLogicalPosition(position), ScrollType.RELATIVE)
      }
    }

    currentProject.getTextEditorsOfPath(path).map(_.getEditor).foreach { editor =>
      invokeLater {
        val ex = editor.asInstanceOf[EditorEx]
        if (currentProject.projectInfo.exists(_.isCaretSharing)) {
          scrollToCaret(ex, offset)
        }
        createPairCaretInEditor(ex, offset).repaint()
      }
    }
  }

  private def showErrorDialog(res: ServerErrorResponse) {
    currentProject.showErrorDialog("Get error message from server", res.message)
  }

  private def handleServerStatusResponse(res: ServerStatusResponse) {
    currentProject.serverStatus = Some(res)
  }

}

trait SelectionEventHandler extends InvokeLater with AppLogger with PublishEvents with HighlightSupport {
  this: CurrentProjectHolder =>
  private val key = new Key[RangeHighlighter]("pair-selection-highlighter")

  def highlightPairSelection(event: SelectContentEvent) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      invokeLater {
        removeOldHighlighter(key, editor)
        val (start, end) = (event.offset, event.offset + event.length)
        if (start != end) {
          val attrs = new TextAttributes(null, Color.GREEN, null, null, 0)
          newHighlight(key, editor, attrs, start, end)
        }
      }
    }
  }

}

trait HighlightSupport {

  def newHighlight(key: Key[RangeHighlighter], editor: TextEditor, attrs: TextAttributes, start: Int, end: Int) = {
    val newHL = editor.getEditor.getMarkupModel.addRangeHighlighter(start, end,
      HighlighterLayer.LAST + 1, attrs, HighlighterTargetArea.EXACT_RANGE)
    editor.putUserData(key, newHL)
  }

  def removeOldHighlighter(key: Key[RangeHighlighter], editor: TextEditor): Option[(Int, Int)] = {
    Option(editor.getUserData(key)).map { hl =>
      val oldRange = (hl.getStartOffset, hl.getEndOffset)
      editor.getEditor.getMarkupModel.removeHighlighter(hl)
      Some(oldRange)
    }.getOrElse(None)
  }

}

trait ChangeContentEventHandler extends InvokeLater with AppLogger with PublishEvents with HighlightSupport {
  this: CurrentProjectHolder =>

  private val changeContentHighlighterKey = new Key[RangeHighlighter]("pair-change-content-highlighter")

  def handleChangeContentEvent(event: ChangeContentEvent) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      runWriteAction {
        try {
          editor.getEditor.getDocument.replaceString(event.offset, event.offset + event.oldFragment.length, event.newFragment)

          val oldRange = removeOldHighlighter(changeContentHighlighterKey, editor)

          val (start, end) = (event.offset, event.offset + event.newFragment.length)
          val (newStart, newEnd) = oldRange.filter(range => start <= range._2 && end >= range._1)
            .map(range => (math.min(start, range._1), math.max(end, range._2)))
            .getOrElse((start, end))

          newHighlight(changeContentHighlighterKey, editor, new TextAttributes(Color.GREEN, Color.YELLOW, null, null, 0),
            newStart, newEnd)
        } catch {
          case e: Throwable => publishEvent(ResetContentRequest(event.path))
        }
      }
    }
  }


}

trait TabEventHandler extends InvokeLater with AppLogger with PublishEvents {
  this: CurrentProjectHolder with PublishSyncFilesRequest =>

  def handleOpenTabEvent(path: String) = {
    openTab(path)(currentProject)
  }

  def handleCloseTabEvent(path: String) = {
    currentProject.getFileByRelative(path).foreach(file => invokeLater(currentProject.fileEditorManager.closeFile(file)))
  }

  private def openTab(path: String)(project: RichProject) {
    currentProject.getFileByRelative(path) match {
      case Some(file) =>
        val openFileDescriptor = project.openFileDescriptor(file)
        if (openFileDescriptor.canNavigate) {
          invokeLater(openFileDescriptor.navigate(true))
        }
      case _ => invokeLater {
        publishSyncFilesRequest()
        publishEvent(ResetTabRequest)
      }
    }
  }

}

trait InvokeLater {
  def invokeLater(f: => Any) {
    ApplicationManager.getApplication.invokeLater(new Runnable {
      override def run(): Unit = f
    })
  }

  def runWriteAction(f: => Any) {
    WriteCommandAction.runWriteCommandAction(null, new Runnable {
      override def run() {
        f
      }
    })
  }

  def runReadAction(f: => Any) {
    ApplicationManager.getApplication.runReadAction(new Runnable {
      override def run() {
        f
      }
    })
  }
}

trait ResetContentEventHandler extends InvokeLater with AppLogger {
  this: CurrentProjectHolder =>

  def handleResetContentEvent(event: ResetContentEvent) = {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      runWriteAction {
        editor.getEditor.getDocument.setText(event.content)
      }
    }
  }

}

trait PublishSyncFilesRequest extends PublishEvents {
  this: CurrentProjectHolder =>
  def publishSyncFilesRequest(): Unit = {
    val files = currentProject.getAllPairableFiles(currentProject.projectInfo.map(_.ignoredFiles).getOrElse(Nil)).map(currentProject.getFileSummary)
    publishEvent(SyncFilesRequest(currentProject.clientInfo.get.clientId, files))
  }

}

trait DialogsCreator {
  this: CurrentProjectHolder =>
  def createJoinProjectDialog(message: Option[String]) = new JoinProjectDialog(currentProject, message)
  def createWorkingModeDialog() = new WorkingModeDialog(currentProject)
}
