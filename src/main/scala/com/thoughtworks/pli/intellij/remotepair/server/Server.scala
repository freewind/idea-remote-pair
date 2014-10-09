package com.thoughtworks.pli.intellij.remotepair.server

import io.netty.bootstrap.ServerBootstrap
import io.netty.channel._
import io.netty.channel.nio.NioEventLoopGroup
import io.netty.channel.socket.SocketChannel
import io.netty.channel.socket.nio.NioServerSocketChannel
import io.netty.handler.codec.LineBasedFrameDecoder
import io.netty.handler.codec.string.{StringEncoder, StringDecoder}
import java.nio.charset.Charset

object Server extends ServerHandlerProvider with ContextHolderProvider with ClientModeGroups with ProjectsHolder {

  override val contexts = new ContextHolder

  def main(args: Array[String]) {
    startServer(8888)
  }

  def startServer(port: Int) {
    val bossGroup = new NioEventLoopGroup()
    val workerGroup = new NioEventLoopGroup()
    try {
      val bootstrap = new ServerBootstrap()
      bootstrap.group(bossGroup, workerGroup)
        .channel(classOf[NioServerSocketChannel])
        .childHandler(ChildHandler)
        .option(ChannelOption.SO_BACKLOG.asInstanceOf[ChannelOption[Any]], 128)
        .childOption(ChannelOption.SO_KEEPALIVE.asInstanceOf[ChannelOption[Any]], true)

      val server = bootstrap.bind(port).sync()
      server.channel().closeFuture().sync()
    } finally {
      workerGroup.shutdownGracefully()
      bossGroup.shutdownGracefully()
    }
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





