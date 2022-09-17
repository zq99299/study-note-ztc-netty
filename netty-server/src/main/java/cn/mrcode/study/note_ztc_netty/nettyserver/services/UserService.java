package cn.mrcode.study.note_ztc_netty.nettyserver.services;

import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Cmd;
import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Module;
import org.springframework.stereotype.Service;

/**
 * @author mrcode
 * @date 2022/9/17 14:34
 */
@Service
@Module("UserService")
public class UserService {
    @Cmd("save")
    public Object save() {
        // 占位
        return null;
    }

    @Cmd("update")
    public Object update() {
        return null;
    }
}
