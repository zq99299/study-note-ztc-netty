package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.provider.test;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.HelloService;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.invoke.User;
import io.protostuff.Service;
import lombok.Data;

/**
 * @author mrcode
 * @date 2022/9/28 22:01
 */
public class HelloServiceImpl implements HelloService {
    private String appName;

    public HelloServiceImpl(String appName) {
        this.appName = appName;
    }

    @Override
    public String hello(String name) {
        if ("异常".equals(name)) {
            throw new RuntimeException("模拟异常");
        }
        return appName + "：" + name;
    }

    @Override
    public String hello(User user) {
        return appName + "：" + user.getName();
    }
}
