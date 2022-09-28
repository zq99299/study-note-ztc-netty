package cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.provider.test;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.HelloService;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke.User;

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
