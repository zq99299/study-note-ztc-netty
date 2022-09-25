package cn.mrcode.study.note_ztc_netty.rapid.rpc.client;

import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcRequest;
import cn.mrcode.study.note_ztc_netty.rapid.rpc.codec.RpcResponse;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.locks.AbstractQueuedSynchronizer;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author mrcode
 * @date 2022/9/25 21:13
 */
@Slf4j
public class RpcFuture implements Future<Object> {
    private RpcRequest request;

    private RpcResponse response;

    // 发起调用的开始时间
    private long startTime;

    private Sync sync;

    // RPC 调用阈值
    private static final long TIME_THRESHOLD = 5000;

    // 等待回调集合
    private List<RpcCallback> pendingCallbacks = new ArrayList<>();

    private ReentrantLock callbackLock = new ReentrantLock();

    private ThreadPoolExecutor executor = new ThreadPoolExecutor(16, 16, 60, TimeUnit.SECONDS, new ArrayBlockingQueue<>(100));

    public RpcFuture(RpcRequest request) {
        this.request = request;
        this.startTime = System.currentTimeMillis();
        // 初始化锁
        this.sync = new Sync();
    }

    /**
     * 响应结果: 实际的回调处理
     * <pre>
     *     RpcFuture future = sendRequest(RpcRequest request)
     *
     * </pre>
     *
     * @param msg
     */
    public void done(RpcResponse msg) {
        this.response = msg;
        // 释放许可
        boolean success = sync.release(1);
        if (success) {
            invokeCallbacks();
        }
        // 花费的时间：整体 RPC 调用的时间
        long costTime = System.currentTimeMillis() - startTime;
        // 如果执行时间大于阈值，说明耗时时间太长了，给一个日志信息
        if (costTime > TIME_THRESHOLD) {
            log.warn("RPC 执行时间太长，requestId={}", request.getRequestId());
        }
    }

    private void invokeCallbacks() {
        callbackLock.lock();
        try {
            for (RpcCallback pendingCallback : pendingCallbacks) {
                runCallback(pendingCallback);
            }
        } finally {
            callbackLock.unlock();
        }
    }

    private void runCallback(RpcCallback pendingCallback) {
        RpcResponse response = this.response;
        executor.submit(() -> {
            // 如果没有异常，则说明调用成功
            if (response.getThrowable() == null) {
                pendingCallback.success(response.getResult());
            } else {
                pendingCallback.failure(response.getThrowable());
            }
        });
    }

    /**
     * 不支持取消功能
     *
     * @param mayInterruptIfRunning {@code true} if the thread executing this
     *                              task should be interrupted; otherwise, in-progress tasks are allowed
     *                              to complete
     * @return
     */
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
    public Object get() throws InterruptedException, ExecutionException {
        // 获取许可：这里传入的参数不重要，sync 的实现并没有使用这里的值
        sync.acquire(-1);
        if (response != null) {
            return response.getResult();
        }
        return null;
    }

    @Override
    public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
        // 成功拿到许可
        boolean success = sync.tryAcquireNanos(-1, unit.toNanos(timeout));
        if (success) {
            if (response != null) {
                return response.getResult();
            }
        }
        throw new RuntimeException("等待超时：requestId=" + request.getRequestId());
    }

    class Sync extends AbstractQueuedSynchronizer {

        // 定义标记：1 已完成，2 等待（阻塞）
        private final int done = 1;
        private final int pending = 0;

        // 获取许可
        @Override
        protected boolean tryAcquire(int arg) {
            return getState() == done;
        }

        // 释放许可
        @Override
        protected boolean tryRelease(int arg) {
            if (getState() == pending) {
                // 如果期望的 state 是 pending，那么就将 state 状态修改为 done
                // 如果修改成功，则返回 true
                if (compareAndSetState(pending, done)) {
                    return true;
                }
            }
            return false;
        }

        public boolean isDone() {
            return getState() == done;
        }
    }
}
