package com.zs.nio.echoclient;

import io.netty.bootstrap.Bootstrap;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.CharsetUtil;

import java.net.InetSocketAddress;
import java.util.concurrent.TimeUnit;

/**
 * Listing 2.4 Main class for the client
 *
 * @author <a href="mailto:norman.maurer@gmail.com">Norman Maurer</a>
 */

public class EchoClient {
    private final String host;

    private final int port;

    public EchoClient(String host, int port) {
        this.host = host;
        this.port = port;
    }

    public void start() throws Exception {
        EventLoopGroup group = new NioEventLoopGroup(1);
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new EchoClientHandler());
                        }
                    });
            for (int i = 0; i < 10; i++) {
                ChannelFuture f = b.connect().sync();

                TimeUnit.SECONDS.sleep(2);

                f.channel().closeFuture().sync();
            }
        } finally {
            group.shutdownGracefully().sync();
        }
    }

    public static void main(String[] args) throws Exception {
        // if (args.length != 2) {
        // System.err.println("Usage: " + EchoClient.class.getSimpleName() + "
        // <host> <port>");
        // return;
        // }
        //
        // final String host = args[0];
        // final int port = Integer.parseInt(args[1]);
        new EchoClient("localhost", 9099).start();
    }

    private class EchoClientHandler extends SimpleChannelInboundHandler<ByteBuf> {
        // —在到服务器的连接已经建立之后将被调用
        @Override
        public void channelActive(ChannelHandlerContext ctx) {
            ctx.writeAndFlush(Unpooled.copiedBuffer("Netty rocks!", CharsetUtil.UTF_8));
        }

        // 当从服务器接收到一条消息时被调用；
        @Override
        public void channelRead0(ChannelHandlerContext ctx, ByteBuf in) {
            System.out.println("Client received: " + in.toString(CharsetUtil.UTF_8));
        }

        // 在处理过程中引发异常时被调用。
        @Override
        public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
            cause.printStackTrace();
            ctx.close();
        }
    }

}
