package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

/**
 * 回调函数接口
 * @author mrcode
 * @date 2022/9/25 21:22
 */
public interface RpcCallback {
    void success(Object result);

    void failure(Throwable cause);
}
