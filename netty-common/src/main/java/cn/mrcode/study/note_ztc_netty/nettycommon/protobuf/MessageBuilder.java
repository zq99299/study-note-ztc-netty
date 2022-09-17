package cn.mrcode.study.note_ztc_netty.nettycommon.protobuf;

import com.google.protobuf.GeneratedMessageV3;

/**
 * @author mrcode
 * @date 2022/9/17 16:31
 */
public class MessageBuilder {
    private static int CRCCODE;

    /**
     * 构建一个响应对象
     *
     * @param module
     * @param cmd
     * @param r
     * @return
     */
    public static MessageModule.Message response(String module, String cmd, Result r) {
        return MessageModule.Message.newBuilder()
                .setCrcCode(CRCCODE)
                .setModule(module)
                .setCmd(cmd)
                .setMessageType(MessageModule.MessageType.RESPONSE)
                .setResultType(r.getResultType())
                .setBody(r.getBody().toByteString())
                .build();
    }

    /**
     * 构建一个请求对象
     *
     * @param module
     * @param cmd
     * @param data
     * @return
     */
    public static MessageModule.Message request(String module, String cmd, GeneratedMessageV3 data) {
        return MessageModule.Message.newBuilder()
                .setCrcCode(CRCCODE)
                .setModule(module)
                .setCmd(cmd)
                .setMessageType(MessageModule.MessageType.REQUEST)
                .setBody(data.toByteString())
                .build();
    }
}
