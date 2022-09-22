package cn.mrcode.study.note_ztc_netty.rapid.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

/**
 * 自定义编码器
 * @author mrcode
 * @date 2022/9/22 21:43
 */
public class RpcEncoder extends MessageToByteEncoder<Object> {
    private Class<?> genericClass;

    public RpcEncoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    /**
     * @param ctx the {@link ChannelHandlerContext} which this {@link MessageToByteEncoder} belongs to
     * @param msg the message to encode
     * @param out the {@link ByteBuf} into which the encoded message will be written
     * @throws Exception
     */

    @Override
    protected void encode(ChannelHandlerContext ctx, Object msg, ByteBuf out) throws Exception {
        if (!genericClass.isInstance(msg)) {
            throw new IllegalArgumentException("不支持该类");
        }

        // 消息分为：包头、包体
        // 这里包头就写字节长度
        byte[] data = ProtostuffUtils.serialize(msg);
        out.writeInt(data.length);
        out.writeBytes(data);
    }
}
