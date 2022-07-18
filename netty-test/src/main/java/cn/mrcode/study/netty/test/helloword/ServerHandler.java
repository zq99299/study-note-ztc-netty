package cn.mrcode.study.netty.test.helloword;

import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.nio.charset.StandardCharsets;

/**
 * @author mrcode
 * @date 2022/7/18 21:24
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    /**
     * 通道激活
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        System.err.println("通道激活");
    }

    /**
     * 读写数据核心方法：每当从客户端接收到新数据时，都会调用该方法
     *
     * @param ctx
     * @param msg
     * @throws Exception
     */
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 读取客户端的数据（缓存中去取，并打印到控制台）
        try {
            ByteBuf buf = (ByteBuf) msg;
            byte[] request = new byte[buf.readableBytes()];
            buf.readBytes(request);
            String requestBody = new String(request, StandardCharsets.UTF_8);
            System.out.println("服务端收到的数据：" + requestBody);

            // 返回响应数据
            String responseBody = "返回响应数据， " + requestBody;
            ctx.writeAndFlush(Unpooled.copiedBuffer(responseBody.getBytes(StandardCharsets.UTF_8)));
        } finally {
            // ByteBuf 是一个引用计数的对象，必须通过该 release() 方法显式释放
            ReferenceCountUtil.release(msg);
        }
    }

    /**
     * netty 由于 IO 错误或处理事件时抛出的异常时，都会回调该方法
     * @param ctx
     * @param cause
     * @throws Exception
     */
    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.fireExceptionCaught(cause);
        // 在大多数情况下，应记录捕获的异常并在此处关闭其关联的通道，尽管此方法的实现可能会有所不同，具体取决于您要如何处理异常情况。
        // 例如，您可能希望在关闭连接之前发送带有错误代码的响应消息。
    }
}
