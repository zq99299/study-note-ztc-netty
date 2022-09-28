package cn.mrcode.study.note_ztc_netty.rapid.rpc.config.provider;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.config.RpcConfigAbstract;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * @author mrcode
 * @date 2022/9/22 22:49
 */
@Data
@EqualsAndHashCode(callSuper = true)
public class ProviderConfig extends RpcConfigAbstract {
    // 程序对象
    private Object ref;
}
