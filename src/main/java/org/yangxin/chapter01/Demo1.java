package org.yangxin.chapter01;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * netty意外退出
 * @author yangxin
 * @create 2021.6.1
 */

public class Demo1 {

    static Logger log = LoggerFactory.getLogger(Demo1.class);

    public static void main(String[] args) throws Exception {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup workers = new NioEventLoopGroup();

        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(boss,workers)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ch.pipeline().addLast(new LoggingHandler(LogLevel.INFO));
                        }
                    })
            ;
            /**
             * 1，netty并未启动，执行到这里后main线程退出，netty退出
             */
            ChannelFuture channelFuture = b.bind(8088).sync();

            /**
             * 2，加了异步关闭后，发现netty直接关闭
             */
            channelFuture.channel().closeFuture().addListener(new ChannelFutureListener() {
                @Override
                public void operationComplete(ChannelFuture future) throws Exception {

                    /**
                     * 这些写shutdownGracefully()关闭线程池是有问题的，
                     * jvm的hook异常情况不一定会调用，也不适合做一些
                     * 耗时的操作
                     */
                    log.debug("netty服务端关闭");
                }
            }).sync();/**3，阻塞main线程直到channel关闭，调用Object的wait方法等待唤醒被关闭*/

        } finally {
            /**
             * 教科书式main线程finally方法关闭线程池
             */
            boss.shutdownGracefully();
            workers.shutdownGracefully();

            Runtime.getRuntime().addShutdownHook( new Thread(()->{
                System.out.println("调用hook");
            }));
        }
    }
}
