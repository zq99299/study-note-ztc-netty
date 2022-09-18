package cn.mrcode.study.note_ztc_netty.nettyclient;

import cn.mrcode.study.note_ztc_netty.nettycommon.protobuf.MessageModule;
import cn.mrcode.study.note_ztc_netty.nettycommon.scanner.Invoker;
import cn.mrcode.study.note_ztc_netty.nettycommon.scanner.NettyProcessBeanScanner;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.util.ReferenceCountUtil;

import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author mrcode
 * @date 2022/9/6 21:12
 */
public class ClientHandler extends ChannelInboundHandlerAdapter {
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
        MessageModule.Message response = (MessageModule.Message) msg;
        workerPool.submit(() -> {
            try {
                String module = response.getModule();
                String cmd = response.getCmd();

                // 可以用来判定响应结果成功还是失败
                MessageModule.ResultType resultType = response.getResultType();

                byte[] data = response.getBody().toByteArray();
                // 获取调用器
                Invoker invoker = NettyProcessBeanScanner.getInvoker(module, cmd);
                invoker.invoker(resultType, data);
            } finally {
                ReferenceCountUtil.release(msg);
            }
        });
    }
}
