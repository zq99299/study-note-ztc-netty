package cn.mrcode.study.note_ztc_netty.rapid.rpc.config.consumer;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.config.RpcConfigAbstract;

/**
 * @author mrcode
 * @date 2022/9/24 17:12
 */
public class ConsumerConfig extends RpcConfigAbstract {
    /**
     * 调用方特有
     */
    protected Class<?> proxyClass;
}
