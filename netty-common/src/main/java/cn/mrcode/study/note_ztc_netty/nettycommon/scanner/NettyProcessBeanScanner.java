package cn.mrcode.study.note_ztc_netty.nettycommon.scanner;

import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Cmd;
import cn.mrcode.study.note_ztc_netty.nettycommon.annotation.Module;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.BeanPostProcessor;
import org.springframework.stereotype.Component;

import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author mrcode
 * @date 2022/9/17 14:37
 */
@Component
// BeanPostProcessor 后置处理器
public class NettyProcessBeanScanner implements BeanPostProcessor {
    private static Map<String, Invoker> invokerMap = new ConcurrentHashMap<>();

    // 使用静态方法是为了方便外部调用
    public static Invoker getInvoker(String module, String cmd) {
        String targetMethodKey = module + "::" + cmd;
        return invokerMap.get(targetMethodKey);
    }

    // 当一个 bean 初始化之后，调用
    @Override
    public Object postProcessAfterInitialization(Object bean, String beanName) throws BeansException {
        Class<?> beanClass = bean.getClass();
        if (!beanClass.isAnnotationPresent(Module.class)) {
            // 如果不包含 Module 注解，就不处理
            return bean;
        }
        Module module = beanClass.getAnnotation(Module.class);

        // 然后扫描方法上有 Cmd 注解的方法
        Method[] methods = beanClass.getMethods();
        for (Method method : methods) {
            if (!method.isAnnotationPresent(Cmd.class)) {
                // 如果不包含 cmd 就跳过
                continue;
            }
            Cmd cmd = method.getAnnotation(Cmd.class);
            String targetMethodKey = module.value() + "::" + cmd.value();
            // 通过反射的方式去调用 目标 service 的方法，就是要拿到 Method 和 Service 的实例，就能发起调用了
            invokerMap.put(targetMethodKey, new Invoker(bean, method));
        }
        return bean;
    }
}
