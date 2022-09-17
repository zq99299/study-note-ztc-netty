package cn.mrcode.study.note_ztc_netty.nettycommon.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 模块注解
 *
 * @author mrcode
 * @date 2022/9/17 14:31
 */
// 仅可使用在类上
@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface Module {
    String value();
}
