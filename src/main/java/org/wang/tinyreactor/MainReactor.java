package org.wang.tinyreactor;

import org.wang.tinyreactor.handler.EventHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 主 Reactor
 *
 * @author wangjiabao
 */
public class MainReactor implements Runnable {

    private final Selector selector;
    private final int port;

    public MainReactor(int port) throws IOException {
        this.selector = Selector.open();
        this.port = port;
    }

    public void run() {
        try(ServerSocketChannel serverSocketChannel = ServerSocketChannel.open()) {
            // 非阻塞模式
            serverSocketChannel.configureBlocking(false);
            // 绑定端口
            serverSocketChannel.socket().bind(new InetSocketAddress(port));
            // 注册到多路复用器中，att 是一个 Accept
            serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT, new TinyAcceptor(selector, serverSocketChannel));
            while (!Thread.interrupted()) {
                // 阻塞直到至少有一个通道产生 I/O 事件
                this.selector.select();
                // 获取存在就绪事件的 FD
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
            // 连接事件则分发给 Acceptor
            TinyAcceptor accept = (TinyAcceptor) selectionKey.attachment();
            accept.doAccept();
        }
    }
}


