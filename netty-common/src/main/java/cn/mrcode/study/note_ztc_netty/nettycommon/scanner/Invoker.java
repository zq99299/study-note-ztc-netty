package cn.mrcode.study.note_ztc_netty.nettycommon.scanner;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * 方法执行器
 *
 * @author mrcode
 * @date 2022/9/17 14:59
 */
public class Invoker {
    // 要调用的目标对象
    private Object target;
    // 要调用的目标对象的方法
    private Method method;

    public Invoker(Object target, Method method) {
        this.target = target;
        this.method = method;
    }

    // 调用目标方法
    public Object invoker(Object... args) {
        try {
            return method.invoke(target, args);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(e);
        }
    }
}
