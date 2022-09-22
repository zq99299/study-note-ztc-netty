package cn.mrcode.study.note_ztc_netty.rapid.rpc.client.protostuff;

import io.protostuff.LinkedBuffer;
import io.protostuff.ProtostuffIOUtil;
import io.protostuff.Schema;
import io.protostuff.runtime.RuntimeSchema;

/**
 * Protostuff 工具类，底层使用 protobuf 来实现
 *
 * @author mrcode
 * @date 2022/9/19 22:21
 */
public class ProtostuffUtils {
    // 重用（管理）此缓冲区以避免在每次序列化时分配
    private static LinkedBuffer buffer = LinkedBuffer.allocate(LinkedBuffer.DEFAULT_BUFFER_SIZE);

    /**
     * 序列化
     *
     * @param obj
     * @param <T>
     * @return
     */
    public static <T> byte[] serialize(T obj) {
        Class<T> clazz = (Class<T>) obj.getClass();
        // 默认策略里面就自己实现了一个缓存，所以只要被创建了一次 schema
        // 后面再获取，会走缓存创建好的那个
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        try {
            return ProtostuffIOUtil.toByteArray(obj, schema, buffer);
        } finally {
            buffer.clear();
        }
    }

    /**
     * 反序列化
     *
     * @param data
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T deserialize(byte[] data, Class<T> clazz) {
        // 默认策略里面就自己实现了一个缓存，所以只要被创建了一次 schema
        // 后面再获取，会走缓存创建好的那个
        Schema<T> schema = RuntimeSchema.getSchema(clazz);
        T t = schema.newMessage();
        ProtostuffIOUtil.mergeFrom(data, t, schema);
        return t;
    }
}
