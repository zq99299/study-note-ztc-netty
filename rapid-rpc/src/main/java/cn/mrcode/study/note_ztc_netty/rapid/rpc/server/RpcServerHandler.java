package cn.mrcode.study.note_ztc_netty.rapid.rpc.server;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * @author mrcode
 * @date 2022/9/22 22:39
 */
// 这里的泛型指的是入站，该类也是 inbound 所以对于服务端来说就是 RpcRequest
// 相反，对于客户端来说这里就是 RpcResponse
public class RpcServerHandler extends SimpleChannelInboundHandler<RpcRequest> {
    @Override
    protected void channelRead0(ChannelHandlerContext ctx, RpcRequest msg) throws Exception {

    }
}
