package com.thoughtworks.pli.remotepair.idea

import com.intellij.openapi.editor.EditorModificationUtil
import com.intellij.testFramework.{LightPlatformCodeInsightTestCase, LightPlatformTestCase}
import com.thoughtworks.pli.intellij.remotepair.protocol.CreateProjectRequest
import com.thoughtworks.pli.intellij.remotepair.server.Server
import com.thoughtworks.pli.remotepair.idea.core._
import io.netty.channel.{ChannelHandlerAdapter, ChannelHandlerContext}

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
class MyIdeaTest extends LightPlatformCodeInsightTestCase with InvokeLater with CurrentProjectHolder with PublishEvents {

  private var _currentProject: RichProject = _

  override def currentProject: RichProject = _currentProject


  import com.intellij.testFramework.LightPlatformCodeInsightTestCase._
  import com.intellij.testFramework.LightPlatformTestCase._

  private val port = 18888

  private var server: Option[Server] = None

  private var handler2: ChannelHandlerContext = _

  override def setUp(): Unit = {
    super.setUp()
    _currentProject = Projects.init(getProject)

    startTheServer()

    Thread.sleep(5000)

    val handler = new MyChannelHandler(currentProject) {
      override def channelActive(ctx: ChannelHandlerContext): Unit = {
        super.channelActive(ctx)
        publishEvent(CreateProjectRequest("ttt", "client111"))
      }
      override def channelRead(ctx: ChannelHandlerContext, msg: Any): Unit = {
        super.channelRead(ctx, msg)
        println("############ client1 msg read: " + msg)
      }
    }

    new Client(ServerAddress("localhost", port)).connect(handler)


    new Client(ServerAddress("localhost", port)).connect(new ChannelHandlerAdapter() {

      override def channelActive(ctx: ChannelHandlerContext): Unit = {
        handler2 = ctx
        handler2.writeAndFlush(CreateProjectRequest("ttt", "client222").toMessage)
      }

      override def channelRead(ctx: ChannelHandlerContext, msg: scala.Any): Unit = {
        println("************ client2 msg read: " + msg)
      }

    })

    Thread.sleep(5000)
  }

  override def tearDown(): Unit = {
    Thread.sleep(20000)
    server.foreach(_.close())
    server = None

    _currentProject = null
    super.tearDown()
  }

  def testInsertStringAtCaretNotMovingCaret(): Unit = {
    val fileName: String = getTestName(false) + ".txt"
    System.out.println(fileName)
    configureFromFileText(fileName, "text <caret>")
    EditorModificationUtil.insertStringAtCaret(myEditor, "xx", false, false)
    checkResultByText("text <caret>xx")
  }

  private def startTheServer(): Unit = {
    val thread = new Thread(new Runnable {
      override def run(): Unit = {
        server = Some(new Server(host = None, port))
        server.foreach(_.start())
      }
    })
    thread.setDaemon(true)
    thread.start()
  }
}
