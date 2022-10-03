package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.provider.test;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.HelloService;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.User;
import io.protostuff.Service;

/**
 * @author mrcode
 * @date 2022/9/28 22:01
 */
public class HelloServiceImpl implements HelloService {
    @Override
    public String hello(String name) {
        return "hello" + name;
    }

    @Override
    public String hello(User user) {
        return "hello ! " + user.getName();
    }
}
