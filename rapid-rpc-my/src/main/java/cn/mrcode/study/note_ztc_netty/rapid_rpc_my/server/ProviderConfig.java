package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.server;

import lombok.*;

/**
 * @author mrcode
 * @date 2022/10/4 12:07
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class ProviderConfig {
    // 服务接口实例
    private Object ref;
    // 服务接口类名
    private String className;
}
