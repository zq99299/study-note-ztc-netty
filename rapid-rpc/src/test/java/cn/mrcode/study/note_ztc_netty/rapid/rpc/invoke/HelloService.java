package cn.mrcode.study.note_ztc_netty.rapid.rpc.invoke;

/**
 * @author mrcode
 * @date 2022/9/28 21:59
 */
public interface HelloService {
    String hello(String name);

    String hello(User user);
}
