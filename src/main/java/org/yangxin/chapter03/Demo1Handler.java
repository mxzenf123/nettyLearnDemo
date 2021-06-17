package org.yangxin.chapter03;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author yangxin
 */
public class Demo1Handler extends ChannelInboundHandlerAdapter {
    static Logger log = LoggerFactory.getLogger(org.yangxin.chapter03.Demo1Handler.class);
    private PooledByteBufAllocator allocator = new PooledByteBufAllocator(false);
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {

        ByteBuf bb = (ByteBuf)msg;
        byte[] body = new byte[bb.readableBytes()];
        bb.readBytes(body);
        //这里如果不释放会出现OutOfDirectMemoryError异常
        ReferenceCountUtil.release(bb);

        ctx.channel().eventLoop().submit(() ->{
            ByteBuf respMsg = allocator.heapBuffer(body.length + 1);
            respMsg.writeBytes(body);
            respMsg.writeByte(10);
            ctx.writeAndFlush(respMsg);
            log.debug("响应完毕：" + respMsg.readableBytes());

        });

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        cause.printStackTrace();
        ctx.close();
    }
}
