package cn.mrcode.study.note_ztc_netty.rapid.rpc.codec;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mrcode
 * @date 2022/9/19 21:59
 */
@Data
public class RpcRequest implements Serializable {
    // 做异步的 RPC 请求的时候这个 ID 才能对应上响应结果对应的 调用方
    private String requestId;
    // 要调用远程的服务名称、方法名称、入参类型、入参 信息
    private String className;
    private String methodName;
    private Class<?>[] parameterTypes;
    private Object[] parameters;
}
