package cn.mrcode.study.note_ztc_netty.rapid.rpc.codec;

import lombok.Data;

import java.io.Serializable;

/**
 * @author mrcode
 * @date 2022/9/19 21:59
 */
@Data
public class RpcResponse implements Serializable {
    private String requestId;
    // 响应结果
    private Object result;
    // 如果有异常的话，也带过来
    private Throwable throwable;
}
