package cn.mrcode.study.note_ztc_netty.rapid_rpc_my.client;

import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid_rpc_my.codec.RpcResponse;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;

/**
 * @author mrcode
 * @date 2022/10/3 22:53
 */
public class RpcFuture implements Future<RpcResponse> {
    private Sync sync;
    private RpcResponse response;
    private RpcRequest request;

    public RpcFuture(RpcRequest request) {
        // 初始化锁
        this.sync = new Sync();
        this.request = request;
    }

    @Override
    public boolean cancel(boolean mayInterruptIfRunning) {
        throw new UnsupportedOperationException("不支持该操作");
    }

    @Override
    public boolean isCancelled() {
        throw new UnsupportedOperationException("不支持该操作");
    }

    @Override
    public boolean isDone() {
        return sync.isDone();
    }

    @Override
    public RpcResponse get() throws InterruptedException, ExecutionException {
        // 期望获取到当前的状态是 完成 状态，如果不是则会阻塞等待
        sync.acquire(sync.done);
        return response;
    }

    @Override
    public RpcResponse get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        if (sync.tryAcquireNanos(sync.done, unit.toNanos(timeout))) {
            return response;
        }
        throw new RuntimeException("等待超时：requestId=" + request.getRequestId());
    }

    /**
     * 当 收到 服务端响应的时候，将结果填充并标识已完成
     * @param response
     */
    public void done(RpcResponse response) {
        this.response = response;
        // 标识已完成
        sync.release(sync.done);
    }

    /**
     * 主要利用 AbstractQueuedSynchronizer
     * 实现了一个 阻塞等待 和 等待超时的功能
     */
    class Sync extends AbstractQueuedSynchronizer {
        // 定义标记：1 已完成，2 等待（阻塞）
        public final int done = 1;
        public final int pending = 0;

        @Override
        protected boolean tryAcquire(int done) {
            // 如果当前状态是 已完成，就返回 true
            return getState() == done;
        }

        @Override
        protected boolean tryRelease(int done) {
            // 如果当前状态是等待中，就设置为已完成
            return compareAndSetState(pending, done);
        }

        public boolean isDone() {
            return getState() == done;
        }
    }
}
