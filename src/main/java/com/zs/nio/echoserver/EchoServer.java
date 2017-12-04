package com.zs.nio.echoserver;

import java.net.InetSocketAddress;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;

public class EchoServer {
    private final int port;

    public EchoServer(int port) {
        this.port = port;
    }

    public static void main(String[] args) throws Exception {
        // if (args.length != 1) {
        // System.err.println("Usage: " + EchoServer.class.getSimpleName() + "
        // <port>");
        // return;
        // }
        // int port = Integer.parseInt(9099);
        new EchoServer(9099).start();
    }

    public void start() throws Exception {
        // 1.创建EventLoopGroup:指定了 NioEventLoopGroup 来接受和处理新的连接
        EventLoopGroup acceptor = new NioEventLoopGroup(1);
        EventLoopGroup worker = new NioEventLoopGroup(1);

        try {
            ServerBootstrap b = new ServerBootstrap();// 2.创建ServerBootstrap
            // 3.指定所使用的NIO传输 Channel,并且将 Channel 的类型指定为 NioServerSocketChannel
            b.group(acceptor, worker).channel(NioServerSocketChannel.class)
                    // 4.使用指定的 端口设置套 接字地址:服务器将绑定到这个地址以监听新的连接请求。
                    .localAddress(new InetSocketAddress(port))
                    // 5.添加一个 EchoServerHandler 到子 Channel 的 ChannelPipeline
                    // EchoServerHandler 是@Shareable，所 以我们可以总是使用 同样的实例
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        // ChannelInitializer。这是关键。当一个新的连接
                        // 被接受时，一个新的子 Channel 将会被创建，而 ChannelInitializer 将会把一个你的
                        // EchoServerHandler 的实例添加到该 Channel 的 ChannelPipeline 中
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new SimpleServerHandler());
                        }
                    });

            // 6.异步地绑定服务器； 调用 sync()方法阻塞 等待直到绑定完成
            ChannelFuture f = b.bind().sync();
            System.out.println(EchoServer.class.getName() + " started and listening for connections on "
                    + f.channel().localAddress());
            f.channel().closeFuture().sync();// 7.获取这个channel的closeFuture,并且阻塞当前线程直到它完成channel关闭
        } finally {
            acceptor.shutdownGracefully().sync();// 8.关闭 EventLoopGroup,释放所有的资源
            worker.shutdownGracefully().sync();

        }
    }

    private static class SimpleServerHandler extends ChannelInboundHandlerAdapter {
        @Override
        public void channelActive(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelActive");
        }

        @Override
        public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
            System.out.println("channelRegistered");
        }

        @Override
        public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
            System.out.println("handlerAdded");
        }
    }
}
