package cn.mrcode.study.netty.test.protobuf;

import com.google.protobuf.InvalidProtocolBufferException;
import org.junit.jupiter.api.Test;

import java.util.Arrays;

/**
 * @author mrcode
 * @date 2022/8/30 21:17
 */
class UserModuleTest {
    /**
     * 序列化测试
     */
    @Test
    public void object2BytesTest() {
        byte[] bytes = object2Bytes();
        System.out.println(Arrays.toString(bytes));
    }

    @Test
    public void bytes2ObjectTest() throws InvalidProtocolBufferException {
        byte[] bytes = object2Bytes();
        UserModule.User user = bytes2Object(bytes);
        System.out.println(user);
    }


    /**
     * 序列化
     *
     * @return
     */
    public static byte[] object2Bytes() {
        UserModule.User.Builder builder = UserModule.User.newBuilder();
        builder.setUserId("1001")
                .setAge(30)
                .setName("张三")
                .addFavorite("足球")
                .addFavorite("撸码");
        UserModule.User user = builder.build();
        byte[] bytes = user.toByteArray();
        /*
          序列化机制：
          java 序列化一个 int 类型，会占用 4 个字节长度
          比如 int a=2  和 int a=10000000  都是占用 4 个字节
          而在 protobuf 中，会根据大小去动态伸缩，就不一定会是 4 个字节了
          所以：一般来说 protobuf 序列化后都会比 int 类型（java 序列化）占用字节小
         */
        return bytes;
    }

    /**
     * 反序列化
     *
     * @param data
     * @return
     * @throws InvalidProtocolBufferException
     */
    public UserModule.User bytes2Object(byte[] data) throws InvalidProtocolBufferException {
        return UserModule.User.parseFrom(data);
    }

    /**
     * 序列化机制，int 类型大小对比
     */
    @Test
    public void intByteSizeTest() {
        // <ByteString@5f8edcc5 size=2 contents="\020\001">
        System.out.println(UserModule.User.newBuilder().setAge(1).build().toByteString());
        // <ByteString@68267da0 size=3 contents="\020\350\a">
        System.out.println(UserModule.User.newBuilder().setAge(1000).build().toByteString());
        // <ByteString@2638011 size=6 contents="\020\377\377\377\377\a">
        System.out.println(UserModule.User.newBuilder().setAge(Integer.MAX_VALUE).build().toByteString());
    }
}