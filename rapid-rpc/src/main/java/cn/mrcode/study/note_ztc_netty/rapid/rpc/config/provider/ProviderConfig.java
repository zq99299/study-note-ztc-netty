package cn.mrcode.study.note_ztc_netty.rapid.rpc.config.provider;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.config.RpcConfigAbstract;
import lombok.Data;

/**
 * @author mrcode
 * @date 2022/9/22 22:49
 */
@Data
public class ProviderConfig extends RpcConfigAbstract {
    // 程序对象
    private Object ref;
}
