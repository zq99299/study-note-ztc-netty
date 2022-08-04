package cn.mrcode.study.netty.test.marshalling;

import java.io.Serializable;

/**
 * 服务端响应给客户端的数据包
 *
 * @author mrcode
 * @date 2022/7/30 21:45
 */
public class ResponseData implements Serializable {
    private String id;
    private String name;
    private String responseMessage;

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

    public String getResponseMessage() {
        return responseMessage;
    }

    public void setResponseMessage(String responseMessage) {
        this.responseMessage = responseMessage;
    }

    @Override
    public String toString() {
        return "ResponseData{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", responseMessage='" + responseMessage + '\'' +
                '}';
    }
}
