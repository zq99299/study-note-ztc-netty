package cn.mrcode.study.note_ztc_netty.nettycommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 操作方法注解
 *
 * @author mrcode
 * @date 2022/9/17 14:33
 */
// 仅可用在方法上
@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Cmd {
    String value();
}
