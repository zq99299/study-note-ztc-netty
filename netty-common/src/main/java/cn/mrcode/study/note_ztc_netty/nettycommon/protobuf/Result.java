package cn.mrcode.study.note_ztc_netty.nettycommon.protobuf;

import com.google.protobuf.GeneratedMessageV3;
import lombok.Data;

/**
 * 封装一个统一的响应对象，用于在 servic 方法调用的返回值
 *
 * @author mrcode
 * @date 2022/9/17 15:55
 */
@Data
public class Result<T extends GeneratedMessageV3> {
    // 响应类型
    private MessageModule.ResultType resultType;

    // 定义响应的泛型内容
    // GeneratedMessageV3 是 protobuf 的实体类继承的父类，要是它才表示响应的是一个 protobuf 的类
    // 这个可以看看前面的 Message 类生成 protobuf 的 java 类之后，继承的就是这个 cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageModule.Message
    private T body;

    public static <T extends GeneratedMessageV3> Result<T> success() {
        return success(null);
    }

    public static <T extends GeneratedMessageV3> Result<T> success(T body) {
        Result<T> result = new Result<>();
        result.setResultType(MessageModule.ResultType.SUCCESS);
        result.setBody(body);
        return result;
    }

    public static <T extends GeneratedMessageV3> Result<T> failure() {
        return failure(null);
    }

    public static <T extends GeneratedMessageV3> Result<T> failure(T body) {
        Result<T> result = new Result<>();
        result.setResultType(MessageModule.ResultType.FAILURE);
        result.setBody(body);
        return result;
    }
}
