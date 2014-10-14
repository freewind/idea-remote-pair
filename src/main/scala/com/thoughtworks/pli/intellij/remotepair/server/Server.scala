package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import java.nio.charset.Charset
import scala.collection.mutable

object Server {

  def main(args: Array[String]) {
    val server = new Server
    server.startAndWait(8888)
  }

}

object AppObjects {
  val contexts = mutable.LinkedHashMap.empty[ChannelHandlerContext, ContextData]
  var projects = Map.empty[String, Project]
  var bindModeGroups = List.empty[Set[String]]
  var followModeMap = Map.empty[String, Set[String]]
}

trait Singletons extends ClientModeGroups with ProjectsHolder with ContextHolder {
  def contexts = new Contexts {
    override val contexts = AppObjects.contexts
  }

  def projects = AppObjects.projects

  def projects_=(projects: Map[String, Project]) = AppObjects.projects = projects

  def bindModeGroups = AppObjects.bindModeGroups

  def bindModeGroups_=(groups: List[Set[String]]) = AppObjects.bindModeGroups = groups

  def followModeMap = AppObjects.followModeMap

  def followModeMap_=(map: Map[String, Set[String]]) = AppObjects.followModeMap = map
}

class Server extends ServerHandlerProvider with Singletons {

  private val bossGroup = new NioEventLoopGroup()
  private val workerGroup = new NioEventLoopGroup()
  private val bootstrap = new ServerBootstrap()
  bootstrap.group(bossGroup, workerGroup)
    .channel(classOf[NioServerSocketChannel])
    .childHandler(ChildHandler)
    .option(ChannelOption.SO_BACKLOG.asInstanceOf[ChannelOption[Any]], 128)
    .childOption(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)

  def startAndWait(port: Int) = {
    try {
      val server = start(port)
      server.sync()
      server.channel().closeFuture().sync()
    } finally {
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }
  }

  def start(port: Int) = {
    bootstrap.bind(port)
  }

  object ChildHandler extends ChannelInitializer[SocketChannel] {
    override def initChannel(channel: SocketChannel) {
      channel.pipeline().addLast(
        new LineBasedFrameDecoder(Int.MaxValue),
        new StringDecoder(Charset.forName("UTF-8")),
        new StringEncoder(Charset.forName("UTF-8")),
        createServerHandler())
    }
  }

}





