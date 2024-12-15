package org.wang.tinyreactor.handler;

import java.nio.channels.SocketChannel;

/**
 * IO 事件处理器
 *
 * @author wangjiabao
 */
public interface Handler {

    /**
     * 处理事件
     *
     * @param channel 产生事件通道
     */
    void handleEvent(SocketChannel channel);
}
