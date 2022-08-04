package cn.mrcode.study.netty.test.marshalling;

import io.netty.handler.codec.marshalling.DefaultMarshallerProvider;
import io.netty.handler.codec.marshalling.DefaultUnmarshallerProvider;
import io.netty.handler.codec.marshalling.MarshallingDecoder;
import io.netty.handler.codec.marshalling.MarshallingEncoder;
import org.jboss.marshalling.MarshallerFactory;
import org.jboss.marshalling.Marshalling;
import org.jboss.marshalling.MarshallingConfiguration;

/**
 * marshalling 的编码和解码
 *
 * @author mrcode
 * @date 2022/7/30 21:46
 */
public final class MarshallingCodeCFactory {
    /**
     * 构建解码器
     *
     * @return
     */
    public static MarshallingDecoder buildMarshallingDecoder() {
        // 获取指定协议的 Marshalling 解码器工厂
        // 这里的 serial 也就是 jboss-marshalling-serial 包
        MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
        MarshallingConfiguration config = new MarshallingConfiguration();
        // 设置协议版本号
        config.setVersion(5);

        DefaultUnmarshallerProvider provider = new DefaultUnmarshallerProvider(factory, config);
        // 构建 Netty 的 MarshallingDecoder
        // 后面一个参数是：序列化对象的最大字节长度。如果接收到的对象的长度大于此值，则会引发错误
        MarshallingDecoder decoder = new MarshallingDecoder(provider, 1024 * 1024 * 1);
        return decoder;
    }

    /**
     * 构建 Netty 的编码器
     *
     * @return
     */
    public static MarshallingEncoder buildMarshallingEncoder() {
        MarshallerFactory factory = Marshalling.getProvidedMarshallerFactory("serial");
        MarshallingConfiguration config = new MarshallingConfiguration();
        // 设置协议版本号
        config.setVersion(5);

        DefaultMarshallerProvider provider = new DefaultMarshallerProvider(factory, config);
        MarshallingEncoder encoder = new MarshallingEncoder(provider);
        return encoder;
    }
}
