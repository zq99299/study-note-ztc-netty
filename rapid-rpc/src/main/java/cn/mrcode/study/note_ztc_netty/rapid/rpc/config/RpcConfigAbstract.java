package cn.mrcode.study.note_ztc_netty.rapid.rpc.config;

import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.atomic.AtomicInteger;

/**
 * 公共配置信息
 *
 * @author mrcode
 * @date 2022/9/24 17:10
 */
public abstract class RpcConfigAbstract {
    private AtomicInteger generator = new AtomicInteger(0);

    @Setter
    protected String id;
    @Getter
    @Setter
    protected String interfaceClass;

    public String getId() {
        if (StringUtils.isBlank(id)) {
            id = "rapid-cfg-gen-" + generator.getAndIncrement();
        }
        return id;
    }
}
