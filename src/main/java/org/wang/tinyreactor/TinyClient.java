package org.wang.tinyreactor;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.SocketChannel;
import java.util.Iterator;
import java.util.Set;

public class TinyClient {

    public static void main(String[] args) throws IOException, InterruptedException {

        try (SocketChannel socketChannel = SocketChannel.open();
             Selector selector = Selector.open()) {

            // 非阻塞模式
            socketChannel.configureBlocking(false);
            boolean isConnected = socketChannel.connect(new InetSocketAddress("localhost", 30000));

            // 如果连接没有立即建立，等待直到连接完成
            if (!isConnected) {
                while (!socketChannel.finishConnect()) {
                    System.out.println("Waiting for connection to complete...");
                    Thread.sleep(100);
                }
            }

            System.out.println("Connected to server.");

            // 注册到选择器，监听可写事件，以便我们可以发送数据
            socketChannel.register(selector, SelectionKey.OP_WRITE);

            // 准备发送的数据
            String message = "Hello from client!";
            ByteBuffer writeBuffer = ByteBuffer.wrap(message.getBytes());

            // 发送数据给服务器
            while (writeBuffer.hasRemaining()) {
                socketChannel.write(writeBuffer);
            }

            // 改为监听可读事件
            socketChannel.register(selector, SelectionKey.OP_READ);

            // 持续监听读取事件
            while (true) {
                selector.select(); // 等待事件发生

                Set<SelectionKey> selectedKeys = selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectedKeys.iterator();

                while (iterator.hasNext()) {
                    SelectionKey key = iterator.next();
                    if (key.isReadable()) {
                        // 处理读取事件
                        handleRead(key);
                    }
                    iterator.remove();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        Thread.sleep(60000);
    }

    private static void handleRead(SelectionKey key) throws IOException {
        SocketChannel socketChannel = (SocketChannel) key.channel();
        ByteBuffer readBuffer = ByteBuffer.allocate(256);
        int bytesRead;

        try {
            bytesRead = socketChannel.read(readBuffer);
        } catch (IOException e) {
            // 连接关闭或出现其他 I/O 错误
            System.out.println("Client disconnected or error occurred.");
            key.cancel();
            socketChannel.close();
            return;
        }

        if (bytesRead == -1) {
            // 远程主机关闭了连接
            System.out.println("Connection closed by server.");
            key.cancel();
            socketChannel.close();
            return;
        }

        // 将缓冲区翻转为读取模式
        readBuffer.flip();

        byte[] data = new byte[readBuffer.remaining()];
        readBuffer.get(data);
        String receivedMessage = new String(data).trim();

        System.out.println("Received from server: " + receivedMessage);

        // 清空缓冲区，准备下一次读取
        readBuffer.clear();
    }
}