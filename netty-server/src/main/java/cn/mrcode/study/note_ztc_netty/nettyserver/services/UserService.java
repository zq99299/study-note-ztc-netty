package cn.mrcode.study.note_ztc_netty.nettyserver.services;

import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Cmd;
import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Module;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.Result;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.UserModule;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.stereotype.Service;

import java.sql.SQLOutput;

/**
 * @author mrcode
 * @date 2022/9/17 14:34
 */
@Service
@Module("UserService")
public class UserService {
    @Cmd("save")
    public Result save(byte[] data) throws InvalidProtocolBufferException {
        UserModule.User user = UserModule.User.parseFrom(data);
        System.out.println("userService:save :" + user);
        // 处理后返回处理结果
        return Result.success(user);
    }

    @Cmd("update")
    public Object update(byte[] data) throws InvalidProtocolBufferException {
        UserModule.User user = UserModule.User.parseFrom(data);
        System.out.println("userService:update :" + user);
        // 处理后返回处理结果
        return Result.success(user);
    }
}
