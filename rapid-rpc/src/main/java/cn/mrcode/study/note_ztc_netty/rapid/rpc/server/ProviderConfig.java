package cn.mrcode.study.note_ztc_netty.rapid.rpc.server;

import lombok.Data;

/**
 * @author mrcode
 * @date 2022/9/22 22:49
 */
@Data
public class ProviderConfig {
    // 接口名称
    private String name;
    // 程序对象
    private Object target;
}
