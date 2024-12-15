package org.wang.tinyreactor;

import lombok.Data;
import org.wang.tinyreactor.handler.EventHandler;

import java.io.IOException;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.nio.channels.SocketChannel;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * @author wangjiabao
 */
@Data
public class TinyAcceptor {

    public static final int DEFAULT_SUB_REACTOR_NUM = 4;

    private Selector selector;
    private ServerSocketChannel serverSocketChannel;
    // 从 Reactor
    private final List<SubReactor> subReactorList = new ArrayList<>(DEFAULT_SUB_REACTOR_NUM);

    // 创建一个具有 4 核心线程、最大 10 线程的线程池
    private static final ThreadPoolExecutor threadPool = new ThreadPoolExecutor(
            4, // 核心线程数
            10, // 最大线程数
            60L, TimeUnit.SECONDS, // 当超过核心线程闲置时的存活时间
            new LinkedBlockingQueue<Runnable>(), // 任务队列
            new ThreadPoolExecutor.CallerRunsPolicy() // 拒绝策略
    );

    public TinyAcceptor(Selector selector, ServerSocketChannel serverSocketChannel) throws IOException {
        this.selector = selector;
        this.serverSocketChannel = serverSocketChannel;

        // init subReactor
        for (int i = 0; i < DEFAULT_SUB_REACTOR_NUM; i++) {
            SubReactor subReactor = new SubReactor();
            subReactorList.add(subReactor);
            // 加入到线程池中启动
            threadPool.execute(subReactor);
        }
    }

    public void doAccept() throws IOException {
        // 获取到客户端连接
        SocketChannel socketChannel = this.serverSocketChannel.accept();
        socketChannel.configureBlocking(false);
        // 选择一个 从 Reactor
        SubReactor subReactor = LoadBalance.getSubReactor(subReactorList);
        // 唤醒 select()
        subReactor.getSelector().wakeup();
        // 注册读写事件
        socketChannel.register(subReactor.getSelector(), SelectionKey.OP_READ | SelectionKey.OP_WRITE);
    }
}
