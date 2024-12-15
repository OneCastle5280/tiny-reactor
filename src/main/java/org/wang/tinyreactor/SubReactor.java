package org.wang.tinyreactor;

import lombok.Data;
import org.wang.tinyreactor.handler.EventHandler;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.channels.SelectionKey;
import java.nio.channels.Selector;
import java.nio.channels.ServerSocketChannel;
import java.util.Iterator;
import java.util.Set;

/**
 * 从 Reactor
 *
 * @author wangjaibao
 */
@Data
public class SubReactor implements Runnable {

    private final Selector selector;

    public SubReactor() throws IOException {
        this.selector = Selector.open();
    }

    public void run() {
        while (!Thread.interrupted()) {
            try {
                this.selector.select();
                Set<SelectionKey> selectionKeys = this.selector.selectedKeys();
                Iterator<SelectionKey> iterator = selectionKeys.iterator();
                while (iterator.hasNext()) {
                    SelectionKey selectionKey = iterator.next();
                    // 分发
                    this.dispatch(selectionKey);
                    iterator.remove();
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * 根据不同的 IO 事件类型进行分发处理
     *
     * @param selectionKey
     * @throws IOException
     */
    private void dispatch(SelectionKey selectionKey) throws IOException {
        // 从 Reactor 只有读写事件
       if (selectionKey.isReadable()) {
            // 可读事件
            EventHandler eventHandler = (EventHandler) selectionKey.attachment();
            eventHandler.handle(SelectionKey.OP_READ);
        } else if (selectionKey.isWritable()) {
            // 可写事件
            EventHandler eventHandler = (EventHandler) selectionKey.attachment();
            eventHandler.handle(SelectionKey.OP_WRITE);
        }
    }

}
