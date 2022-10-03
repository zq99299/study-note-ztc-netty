package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke;

import lombok.Builder;
import lombok.Data;

/**
 * @author mrcode
 * @date 2022/9/19 22:18
 */
@Data
@Builder
public class User {
    private String id;
    private String name;
    private Integer age;
    private String desc;
}
