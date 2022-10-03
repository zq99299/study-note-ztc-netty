package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client;

import lombok.*;

/**
 * 客户端连接配置
 *
 * @author mrcode
 * @date 2022/10/3 18:43
 */
@Data
@ToString
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class RpcClientConfig {
    private String appName;
    private String host;
    private int port;
}
