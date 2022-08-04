package cn.mrcode.study.netty.test.marshalling;

import java.io.Serializable;
import java.util.Arrays;

/**
 * Client 发送数据给 Server 的数据包
 *
 * @author mrcode
 * @date 2022/7/30 21:45
 */
// Marshalling 要求被序列化的类需要实现 jdk 的 Serializable 接口
public class RequestData implements Serializable {

    private String id;
    private String name;
    private String requestMessage;
    private byte[] attachment;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getRequestMessage() {
        return requestMessage;
    }

    public void setRequestMessage(String requestMessage) {
        this.requestMessage = requestMessage;
    }

    public byte[] getAttachment() {
        return attachment;
    }

    public void setAttachment(byte[] attachment) {
        this.attachment = attachment;
    }

    @Override
    public String toString() {
        return "RequestData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", requestMessage='" + requestMessage + '\'' +
                ", attachment=" + Arrays.toString(attachment) +
                '}';
    }
}
