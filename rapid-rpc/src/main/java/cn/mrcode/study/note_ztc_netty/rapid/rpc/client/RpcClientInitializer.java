package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcDecoder;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcEncoder;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcResponse;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.LengthFieldBasedFrameDecoder;

/**
 * @author mrcode
 * @date 2022/9/18 18:18
 */
public class RpcClientInitializer extends ChannelInitializer<SocketChannel> {
    @Override
    protected void initChannel(SocketChannel ch) throws Exception {
        ch.pipeline()
                .addLast(new RpcEncoder(RpcRequest.class))
                .addLast(new LengthFieldBasedFrameDecoder(
                        // 数据包最大长度
                        65536,
                        // 长度字段的偏移量
                        0,
                        // 长度字段的长度
                        4,
                        // 添加到长度字段值的补偿
                        0,
                        // 从解码帧中剥离的第一个字节数
                        0
                ))
                .addLast(new RpcDecoder(RpcResponse.class))
                .addLast(new RpcClientHandler());
    }
}
