package cn.mrcode.study.note_ztc_netty.rapid.rpc.codec;

import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;

import java.util.List;

/**
 * @author mrcode
 * @date 2022/9/22 21:43
 */
public class RpcDecoder extends ByteToMessageDecoder {
    private Class<?> genericClass;

    public RpcDecoder(Class<?> genericClass) {
        this.genericClass = genericClass;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) throws Exception {
        if (!in.isReadable(4)) {
            // 如果当前 buf 小于 4 个字节，则不读取
            // 因为在写数据包的时候一个头就是 4 个字节
            return;
        }
        in.markReaderIndex();
        // 获取数据长度
        int dataLength = in.readInt();
        // 如果可读数据小于 数据长度
        if (in.readableBytes() < dataLength) {
            // 数据包还没有传完，等待下一次可读的时候再读
            in.resetReaderIndex();
            return;
        }

        byte[] data = new byte[dataLength];
        // 读取字节到数组中
        in.readBytes(data);

        Object result = ProtostuffUtils.deserialize(data, genericClass);
        out.add(result);
    }
}
