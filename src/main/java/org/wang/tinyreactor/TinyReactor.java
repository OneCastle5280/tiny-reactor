package org.wang.tinyreactor;


import lombok.Data;
import org.wang.tinyreactor.handler.EventHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author wangjiabao
 */
@Data
public class TinyReactor implements Runnable {

    private final Selector selector;
    private final int port;

    public TinyReactor(int port) throws IOException {
        this.selector = Selector.open();
        this.port = port;
    }

    public void run() {
        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // 非阻塞
            serverSocketChannel.configureBlocking(false);
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            // 注册
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new TinyAcceptor(selector, serverSocketChannel));

            while (!Thread.currentThread().isInterrupted()) {
                int select = this.selector.select();
                if (select == 0) {
                    System.out.println("selector is empty");
                    continue;
                }
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 分发
                    this.dispatch(selectionKey);
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    /**
     * 根据不同的 IO 事件类型进行分发处理
     *
     * @param selectionKey
     * @throws IOException
     */
    private void dispatch(SelectionKey selectionKey) throws IOException {
        if (selectionKey.isAcceptable()) {
            // 连接事件
            TinyAcceptor accept = (TinyAcceptor) selectionKey.attachment();
            accept.doAccept();
        } else if (selectionKey.isReadable()) {
            // 可读事件
            EventHandler eventHandler = (EventHandler) selectionKey.attachment();
            eventHandler.handle(SelectionKey.OP_READ);
        } else if (selectionKey.isWritable()) {
            // 可写事件
            EventHandler eventHandler = (EventHandler) selectionKey.attachment();
            eventHandler.handle(SelectionKey.OP_WRITE);
        }
    }


    public static void main(String[] args) throws IOException, InterruptedException {
        // 启动服务端监听 30000 端口连接
        new Thread(new TinyReactor(30000)).start();

        Thread.sleep(50000);
    }
}
