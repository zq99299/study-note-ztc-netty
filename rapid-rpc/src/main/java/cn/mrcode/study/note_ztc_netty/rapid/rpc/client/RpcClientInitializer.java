package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;

/**
 * @author mrcode
 * @date 2022/9/18 18:18
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new RpcClientHandler());
    }
}
