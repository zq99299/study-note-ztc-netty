package cn.mrcode.study.note_ztc_netty.rapid.rpc.client.protostuff;

import lombok.Builder;
import lombok.Data;

/**
 * @author mrcode
 * @date 2022/9/19 22:20
 */
@Data
@Builder
public class Group {
    private String id;
    private String name;
    private User user;
}
