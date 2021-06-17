package org.yangxin.chapter03;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Demo1 {
    static Logger log = LoggerFactory.getLogger(org.yangxin.chapter01.Demo1.class);

    public static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup(1);
        EventLoopGroup workers = new NioEventLoopGroup(1);

        ServerBootstrap b = new ServerBootstrap();

        try {
            b.group(boss, workers).channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {

                            ch.pipeline().addLast(new Demo1Handler())
                            ;
                        }
                    });

            ChannelFuture cf = b.bind(8088).sync();
            log.debug("服务端启动完成");
            cf.channel().closeFuture().addListener(ChannelFutureListener.CLOSE).sync();

        } finally {
            boss.shutdownGracefully();
            workers.shutdownGracefully();
        }
    }
}
