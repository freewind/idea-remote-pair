package com.thoughtworks.pli.intellij.remotepair

import java.awt.Color
import java.nio.charset.Charset

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.{Editor, ScrollType}
import com.intellij.openapi.editor.ex.EditorEx
import com.intellij.openapi.editor.markup._
import com.intellij.openapi.util.Key
import com.intellij.openapi.vfs.VirtualFile
import com.thoughtworks.pli.intellij.remotepair.actions.dialogs.{SyncProgressDialog, ComparePairableFilesDialog, JoinProjectDialog, WorkingModeDialog}
import com.thoughtworks.pli.intellij.remotepair.client._
import com.thoughtworks.pli.intellij.remotepair.protocol._
import com.thoughtworks.pli.intellij.remotepair.ui.PairCaretComponent
import com.thoughtworks.pli.intellij.remotepair.utils.{Insert, StringDiff, Md5Support}
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
          log.info(s"Plugin ${currentProject.clientInfo.map(_.name).getOrElse("Unknown")} receives line: $line")
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

  private var workerGroup: NioEventLoopGroup = _
  private var bootstrap: Bootstrap = _

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

trait EventHandler extends TabEventHandler with ChangeContentEventHandler with Md5Support with AppLogger with PublishEvents with DialogsCreator with SelectionEventHandler with PublishSyncFilesRequest with PublishVersionedDocumentEvents with CurrentProjectHolder {

  def handleEvent(event: PairEvent) {
    event match {
      case event: OpenTabEvent => handleOpenTabEvent(event.path)
      case event: CloseTabEvent => handleCloseTabEvent(event.path)
      case event: ChangeContentEvent => handleChangeContentEvent(event)
      case event: ResetTabEvent => handleOpenTabEvent(event.path)
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
      case event: ChangeContentConfirmation => handleChangeContentConfirmation(event)
      case request: CreateServerDocumentRequest => handleCreateServerDocumentRequest(request)
      case event: CreateDocumentConfirmation => handleCreateDocumentConfirmation(event)
      case event: JoinedToProjectEvent => handleJoinedToProjectEvent(event)
      case event: PairableFiles => handlePairableFiles(event)
      case event: GetPairableFilesFromPair => handleGetPairableFilesFromPair(event)
      case _ => log.error("!!!! Can't handle: " + event)
    }
  }

  private def handleGetPairableFilesFromPair(event: GetPairableFilesFromPair): Unit = {
    for {
      myClientId <- currentProject.clientInfo.map(_.clientId)
      fileSummaries = currentProject.getAllPairableFiles(currentProject.ignoredFiles).map(currentProject.getFileSummary)
    } publishEvent(new PairableFiles(myClientId, event.fromClientId, fileSummaries))
  }

  private def handlePairableFiles(event: PairableFiles): Unit = invokeLater {
    for {
      clients <- currentProject.projectInfo.map(_.clients)
      pairName <- clients.find(_.clientId == event.fromClientId).map(_.name)
    } {
      val dialog = new ComparePairableFilesDialog(currentProject)
      dialog.setPairFiles(pairName, event.fileSummaries)
      dialog.show()
    }
  }

  def handleJoinedToProjectEvent(event: JoinedToProjectEvent): Unit = {
    currentProject.getOpenedFiles.foreach(publishCreateDocumentEvent)
  }

  private def handleCreateDocumentConfirmation(event: CreateDocumentConfirmation): Unit = runWriteAction {
    val doc = currentProject.versionedDocuments.getOrCreate(currentProject, event.path)
    doc.synchronized {
      doc.handleCreation(event) match {
        case Some(content) => currentProject.smartSetContentTo(event.path, content)
        case _ => // do nothing
      }
    }
  }

  private def handleCreateServerDocumentRequest(request: CreateServerDocumentRequest): Unit = runReadAction {
    currentProject.getFileByRelative(request.path).map(currentProject.getFileContent).foreach { content =>
      publishEvent(CreateDocument(request.path, content))
    }
  }

  private def handleCreateDirEvent(event: CreateDirEvent): Unit = runWriteAction {
    currentProject.findOrCreateDir(event.path)
  }

  private def handleCreateFileEvent(event: CreateFileEvent): Unit = runWriteAction {
    currentProject.smartSetContentTo(event.path, event.content)
  }

  private def handleDeleteFileEvent(event: DeleteFileEvent): Unit = runWriteAction {
    currentProject.deleteFile(event.path)
  }

  private def handleDeleteDirEvent(event: DeleteDirEvent): Unit = runWriteAction {
    currentProject.deleteDir(event.path)
  }

  private def handleSyncFileEvent(event: SyncFileEvent): Unit = {
    invokeLater {
      val holder = SyncProgressDialogHolder
      holder.synchronized {
        holder.progressDialog.foreach { dialog =>
          dialog.completeFile(event.path) {
            holder.progressDialog = None
          }
        }
      }
    }
    runWriteAction(currentProject.smartSetContentTo(event.path, event.content))
  }

  private def handleSyncFilesRequest(req: SyncFilesRequest): Unit = {
    val files = currentProject.getAllPairableFiles(currentProject.ignoredFiles)
    val diffs = calcDifferentFiles(files, req.fileSummaries)
    val myClientId = currentProject.clientInfo.map(_.clientId).get
    publishEvent(MasterPairableFiles(myClientId, req.fromClientId, files.map(currentProject.getRelativePath), diffs.length))
    diffs.foreach(file => publishEvent(SyncFileEvent(myClientId, req.fromClientId, currentProject.getRelativePath(file), currentProject.getFileContent(file))))
  }

  private def handleSyncFilesForAll(): Unit = invokeLater {
    publishSyncFilesRequest()
  }

  private def calcDifferentFiles(localFiles: Seq[VirtualFile], fileSummaries: Seq[FileSummary]): Seq[VirtualFile] = {
    def isSameWithRemote(file: VirtualFile) = fileSummaries.contains(currentProject.getFileSummary(file))
    localFiles.filterNot(isSameWithRemote)
  }

  private def handleMasterPairableFiles(event: MasterPairableFiles): Unit = {
    val ignoredFiles = currentProject.ignoredFiles
    invokeLater {
      if (event.paths.nonEmpty) {
        showProgressDialog(event.diffFiles)
        currentProject.getAllPairableFiles(ignoredFiles).foreach { myFile =>
          if (!event.paths.contains(currentProject.getRelativePath(myFile))) {
            log.info("#### delete file which is not exist on master side: " + myFile.getPath)
            if (myFile.exists()) {
              runWriteAction(myFile.delete(this))
            }
          }
        }
      }
    }
  }


  private def showProgressDialog(total: Int): Unit = SyncProgressDialogHolder.synchronized {
    SyncProgressDialogHolder.progressDialog = Some(new SyncProgressDialog(total))
    SyncProgressDialogHolder.progressDialog.foreach { dialog =>
      new Thread(new Runnable {
        override def run(): Unit = dialog.showIt(currentProject.getWindow())
      }).start()
    }
  }

  private def handleClientInfoResponse(event: ClientInfoResponse) {
    currentProject.clientInfo = Some(event)
  }

  private def handleAskForJoinProject(message: Option[String]) {
    invokeLater(createJoinProjectDialog(message).show())
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

    currentProject.getTextEditorsOfPath(path).foreach { editor =>
      invokeLater {
        try {
          val ex = editor.asInstanceOf[EditorEx]
          if (currentProject.projectInfo.exists(_.isCaretSharing)) {
            scrollToCaret(ex, offset)
          }
          createPairCaretInEditor(ex, offset).repaint()
        } catch {
          case e: Throwable => // FIXME fix it later
          // log.error("Error occurs when moving caret from pair: " + e.toString, e)
        }
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
  private val key = new Key[Seq[RangeHighlighter]]("pair-selection-highlighter")

  def highlightPairSelection(event: SelectContentEvent) {
    currentProject.getTextEditorsOfPath(event.path).foreach { editor =>
      invokeLater {
        try {
          removeOldHighlighters(key, editor)
          val (start, end) = (event.offset, event.offset + event.length)
          if (start != end) {
            val attrs = new TextAttributes(null, Color.GREEN, null, null, 0)
            newHighlights(key, editor, attrs, Seq(Range(start, end)))
          }
        } catch {
          case e: Throwable => log.error("Error occurs when highlighting pair selection: " + e.toString, e)
        }
      }
    }
  }

}

case class Range(start: Int, end: Int)

trait HighlightSupport {

  def newHighlights(key: Key[Seq[RangeHighlighter]], editor: Editor, attrs: TextAttributes, ranges: Seq[Range]) = {
    val newHLs = ranges.map(r => editor.getMarkupModel.addRangeHighlighter(r.start, r.end,
      HighlighterLayer.LAST + 1, attrs, HighlighterTargetArea.EXACT_RANGE))
    editor.putUserData(key, newHLs)
  }

  def removeOldHighlighters(key: Key[Seq[RangeHighlighter]], editor: Editor): Seq[Range] = {
    val oldHLs = Option(editor.getUserData(key)).getOrElse(Nil)
    val oldRanges = oldHLs.map(hl => Range(hl.getStartOffset, hl.getEndOffset))

    oldHLs.foreach(editor.getMarkupModel.removeHighlighter)
    oldRanges
  }

}

trait ChangeContentEventHandler extends InvokeLater with AppLogger with PublishEvents with HighlightSupport {
  this: CurrentProjectHolder =>

  private val changeContentHighlighterKey = new Key[Seq[RangeHighlighter]]("pair-change-content-highlighter")

  def handleChangeContentConfirmation(event: ChangeContentConfirmation): Unit = currentProject.getFileByRelative(event.path).foreach { file =>
    val doc = currentProject.versionedDocuments.get(event.path)
    runWriteAction {
      try {
        doc.synchronized {
          val currentContent = currentProject.smartGetFileContent(file).text
          doc.handleContentChange(event, currentContent).map { targetContent =>
            currentProject.smartSetContentTo(event.path, Content(targetContent, file.getCharset.name()))
            highlightPairChanges(event.path, targetContent)
          }
        }
      } catch {
        case e: Throwable => log.error("Error occurs when handling ChangeContentConfirmation: " + e.toString, e)
      }
    }
  }

  private def highlightPairChanges(path: String, targetContent: String) {
    val attrs = new TextAttributes(Color.GREEN, Color.YELLOW, null, null, 0)
    for {
      editor <- currentProject.getTextEditorsOfPath(path)
      currentContent = editor.getDocument.getCharsSequence.toString
      oldRanges = removeOldHighlighters(changeContentHighlighterKey, editor)
      diffs = StringDiff.diffs(currentContent, targetContent)
      newRanges = diffs.collect {
        case Insert(offset, content) => Range(offset, offset + content.length)
      }
      mergedRanges = mergeRanges(oldRanges, newRanges)
    } newHighlights(changeContentHighlighterKey, editor, attrs, mergedRanges)
  }

  private def mergeRanges(oldRanges: Seq[Range], newRanges: Seq[Range]): Seq[Range] = {
    // FIXME merge them
    newRanges
  }

  def handleChangeContentEvent(event: ChangeContentEvent) {
    // FIXME do nothing, remove later
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

trait PublishSyncFilesRequest extends PublishEvents {
  this: CurrentProjectHolder =>
  def publishSyncFilesRequest(ignoredFiles: Seq[String] = currentProject.ignoredFiles): Unit = {
    val files = currentProject.getAllPairableFiles(ignoredFiles).map(currentProject.getFileSummary)
    publishEvent(SyncFilesRequest(currentProject.clientInfo.get.clientId, files))
  }

}

trait DialogsCreator {
  this: CurrentProjectHolder =>
  def createJoinProjectDialog(message: Option[String]) = new JoinProjectDialog(currentProject, message)
  def createWorkingModeDialog() = new WorkingModeDialog(currentProject)
}
