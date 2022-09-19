package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;

/**
 * @author mrcode
 * @date 2022/9/18 18:04
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<Object> {
    // 该地址链接的通道
    private Channel channel;
    // HOST:PORT
    private SocketAddress socketAddress;

    /**
     * 通道注册
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelRegistered(ChannelHandlerContext ctx) throws Exception {
        super.channelRegistered(ctx);
        Channel channel = ctx.channel();
        this.channel = channel;
        log.info("通道已注册：{}", channel);
    }

    /**
     * 通道被激活时
     *
     * @param ctx
     * @throws Exception
     */
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
        this.socketAddress = channel.remoteAddress();
        log.info("通道已可用：{}", ctx.channel());
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Object msg) throws Exception {

    }

    /**
     * 获取该处理器绑定的远程地址
     *
     * @return
     */
    public SocketAddress remoteAddress() {
        return this.socketAddress;
    }

    public void close() {
        // netty 提供的一种主动关闭连接的机制，发送一个空的 BUFFER 然后 ChannelFutureListener 监听器就会将这个 ChannelFuture 关闭，和释放相关的资源
        channel.writeAndFlush(Unpooled.EMPTY_BUFFER).addListener(ChannelFutureListener.CLOSE);
    }
}
