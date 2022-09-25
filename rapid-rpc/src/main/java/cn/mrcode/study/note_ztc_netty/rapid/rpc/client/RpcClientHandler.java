package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcResponse;
import io.netty.buffer.Unpooled;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import lombok.extern.slf4j.Slf4j;

import java.net.SocketAddress;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mrcode
 * @date 2022/9/18 18:04
 */
@Slf4j
public class RpcClientHandler extends SimpleChannelInboundHandler<RpcResponse> {
    // 该地址链接的通道
    private Channel channel;
    // HOST:PORT
    private SocketAddress socketAddress;

    /**
     * 保存请求关系，响应时能找到响应给哪一个调用者
     */
    private Map<String /* requestId */, RpcFuture> pendingRpcTable = new ConcurrentHashMap<>();

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
    protected void channelRead0(ChannelHandlerContext ctx, RpcResponse msg) throws Exception {
        String requestId = msg.getRequestId();
        RpcFuture rpcFuture = pendingRpcTable.get(requestId);
        if (rpcFuture != null) {
            // 回调结果处理
            rpcFuture.done(msg);
            // 移除映射关系
            pendingRpcTable.remove(requestId);
        }

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

    /**
     * 发布发送请求方法
     *
     * @param request
     * @return
     */
    public RpcFuture sendRequest(RpcRequest request) {
        RpcFuture rpcFuture = new RpcFuture(request);
        pendingRpcTable.put(request.getRequestId(), rpcFuture);
        // 发送请求
        channel.writeAndFlush(request);
        return rpcFuture;
    }
}
