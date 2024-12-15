package org.wang.tinyreactor.handler;

import java.nio.channels.SelectionKey;
import java.nio.channels.SocketChannel;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class EventHandler {

    private final SocketChannel socketChannel;

    // 创建一个具有 2 核心线程、最大 10 线程的线程池
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            2, // 核心线程数
            10, // 最大线程数
            60L, TimeUnit.SECONDS, // 当超过核心线程闲置时的存活时间
            new LinkedBlockingQueue<Runnable>(), // 任务队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );

    public EventHandler(SocketChannel socketChannel) {
        this.socketChannel = socketChannel;
    }

    public void handle(int eventType) {
        if (eventType == SelectionKey.OP_READ) {
            threadPool.submit(new ReadEventHandler(socketChannel)); // 提交读事件处理器到线程池
        } else if (eventType == SelectionKey.OP_WRITE) {
            threadPool.submit(new WriteEventHandler(socketChannel)); // 提交写事件处理器到线程池
        }
    }
}
