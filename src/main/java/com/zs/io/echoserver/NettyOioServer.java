package com.zs.io.echoserver;

import java.net.InetSocketAddress;
import java.nio.charset.Charset;

import com.zs.nio.echoserver.EchoServer;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class NettyOioServer {

    public NettyOioServer(int port) throws InterruptedException {
        final ByteBuf buf = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Hi! \r\n", Charset.forName("UTF-8")));

        // 使用OioEventLoopGroup以允许阻塞模式
        // EventLoopGroup group = new OioEventLoopGroup();
        // 使用NIO
        // EventLoopGroup group = new NioEventLoopGroup();

        // 使用NIO
        NioEventLoopGroup group = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            // Oio:channel(OioServerSocketChannel.class)
            // Nio:channel(NioServerSocketChannel.class)
            // Epoll: channel(EpollServerSocketChannel.class)
            bootstrap.group(group).channel(NioServerSocketChannel.class).localAddress(new InetSocketAddress(port))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new ChannelInboundHandlerAdapter() {
                                @Override
                                public void channelActive(ChannelHandlerContext ctx) throws Exception {
                                    // ctx.fireChannelActive();
                                    // 消息写完后会回调关闭channel
                                    ctx.writeAndFlush(buf.duplicate()).addListener(ChannelFutureListener.CLOSE);
                                }
                            });
                        }
                    });
            ChannelFuture f = bootstrap.bind().sync();
            System.out.println(EchoServer.class.getName() + " started and listening for connections on "
                    + f.channel().localAddress());
            f.channel().closeFuture().sync();
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws InterruptedException {
        new NettyOioServer(9099);
    }
}