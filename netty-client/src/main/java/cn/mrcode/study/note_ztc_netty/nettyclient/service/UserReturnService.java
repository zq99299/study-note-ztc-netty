package cn.mrcode.study.note_ztc_netty.nettyclient.service;

import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Cmd;
import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Module;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageModule;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.Result;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.UserModule;
import com.google.protobuf.InvalidProtocolBufferException;
import org.springframework.stereotype.Service;

/**
 * @author mrcode
 * @date 2022/9/17 20:12
 */
@Service
@Module("UserService")
public class UserReturnService {
    @Cmd("save")
    public void save(MessageModule.ResultType resultType, byte[] data) throws InvalidProtocolBufferException {
        UserModule.User user = UserModule.User.parseFrom(data);
        System.out.println("userReturnService:save :" + user);
    }

    @Cmd("update")
    public void update(MessageModule.ResultType resultType, byte[] data) throws InvalidProtocolBufferException {
        UserModule.User user = UserModule.User.parseFrom(data);
        System.out.println("userReturnService:update :" + user);
    }
}
