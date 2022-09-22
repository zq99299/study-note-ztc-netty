package cn.mrcode.study.note_ztc_netty.rapid.rpc.client.protostuff;

import java.lang.reflect.Array;
import java.sql.SQLOutput;
import java.util.Arrays;

/**
 * @author mrcode
 * @date 2022/9/19 22:21
 */
public class DemoTest {
    public static void main(String[] args) {
        User user = User.builder()
                .id("1")
                .age(20)
                .name("张三")
                .desc("programmer")
                .build();
        Group group = Group.builder()
                .id("1")
                .name("分组 1")
                .user(user)
                .build();

        // 使用 protostuff 序列化和反序列化
        byte[] data = ProtostuffUtils.serialize(group);
        System.out.println("序列化后字节大小：" + data.length);
        System.out.println("序列化后：" + Arrays.toString(data));

        // 反序列化
        Group result = ProtostuffUtils.deserialize(data, Group.class);
        System.out.println("反序列化后：" + result);
    }
}
