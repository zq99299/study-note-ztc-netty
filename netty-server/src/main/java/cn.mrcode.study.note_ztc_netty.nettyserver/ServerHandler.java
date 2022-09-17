package cn.mrcode.study.note_ztc_netty.nettyserver;

import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageBuilder;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageModule;
import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.Result;
import cn.mrcode.study.note_ztc_netty.nettycommon.scanner.Invoker;
import cn.mrcode.study.note_ztc_netty.nettycommon.scanner.NettyProcessBeanScanner;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/9/6 21:12
 */
public class ServerHandler extends ChannelInboundHandlerAdapter {
    ThreadPoolExecutor workerPool = new ThreadPoolExecutor(
            5,  // 核心线程
            10, // 最大线程
            60L, // 线程释放空闲时间，这里是 60 + 后面的单位
            TimeUnit.SECONDS,
            new ArrayBlockingQueue<>(100), // 允许排队的数量
            new ThreadPoolExecutor.DiscardPolicy() // 拒绝策略：DiscardPolicy 是直接丢弃掉被拒绝的任务
    );

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        // 强转为我们统一的数据传输类
        MessageModule.Message request = (MessageModule.Message) msg;
        // 由于这个会被 worker 回调，这个 worker 是管理链接的
        // 所以最好不要在该线程里面去处理一些业务逻辑，而是将他们使用线程异步的去处理业务逻辑
        workerPool.submit(() -> {
            String module = request.getModule();
            String cmd = request.getCmd();

            // 直接拿到请求参数体
            // 这里不对即将要调用的方法进一步处理
            // 因为这里的 service 实现的入参和普通 service 不太一样，你的入参必须是一个 Protobuf 的序列化类
            // 如果非要在这里来构建反序列化，我第一个想法就是拿到入参的类，然后通过反射去调用 parseFrom 方法，比如：UserModule.User.parseFrom(data);
            byte[] data = request.getBody().toByteArray();
            // 获取调用器
            Invoker invoker = NettyProcessBeanScanner.getInvoker(module, cmd);
            Object result = invoker.invoker(data);
            // 由于规定 service 方法必须有响应，并且还必须是固定的结果类，所以这里可以直接强转结果
            Result r = (Result) result;
            MessageModule.Message response = MessageBuilder.response(module, cmd, r);
            // 写回到客户端
            ctx.writeAndFlush(response);
        });
    }
}
