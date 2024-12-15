package org.wang.tinyIoMultiplexing;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.*;
import java.util.Iterator;
import java.util.Set;

/**
 * @author wangjiabao
 */
public class IOMultiplexing {

    public static void main(String[] args) throws IOException {
        // 即 select/poll/epoll
        Selector selector = Selector.open();
        // 打开服务器套接字通道
        ServerSocketChannel serverSocketChannel = ServerSocketChannel.open();
        // 非阻塞模式
        serverSocketChannel.configureBlocking(false);
        // 监听端口
        serverSocketChannel.socket().bind(new InetSocketAddress(30000));
        // 注册到选择器上，监听接受事件
        serverSocketChannel.register(selector, SelectionKey.OP_ACCEPT);
        // 事件循环
        while (Thread.interrupted()) {
            // 阻塞直到至少有一个通道准备就绪
            selector.select();
            // 获取存在事件的 FD
            Set<SelectionKey> selectedKeys = selector.selectedKeys();
            Iterator<SelectionKey> iterator = selectedKeys.iterator();

            while (iterator.hasNext()) {
                SelectionKey key = iterator.next();
                iterator.remove(); // 避免重复处理

                if (key.isAcceptable()) {
                    // 处理客户端连接事件
                    SocketChannel socketChannel = serverSocketChannel.accept();
                    socketChannel.configureBlocking(false);
                    // 将 client 连接注册到 selector 中，统一监听，监听可读事件
                    socketChannel.register(selector, SelectionKey.OP_READ);
                } else if (key.isReadable()) {
                    // 处理可读事件
                }
            }
        }
    }
}
