package cn.mrcode.study.note_ztc_netty.nettyclient;

import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.UserModule;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author mrcode
 * @date 2022/9/17 21:02
 */
@RestController
@RequestMapping("/test")
public class TestController {
    @Autowired
    private Client client;

    @GetMapping("/save")
    public void save() {
        UserModule.User user = UserModule.User.newBuilder()
                .setUserId("0001")
                .setUserName("张三")
                .build();
        client.call("UserService", "save", user);
    }


    @GetMapping("/update")
    public void update() {
        UserModule.User user = UserModule.User.newBuilder()
                .setUserId("0002")
                .setUserName("李四")
                .build();
        client.call("UserService", "update", user);
    }
}
