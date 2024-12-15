package org.wang.tinyreactor.handler;


import lombok.AllArgsConstructor;

import java.nio.channels.SocketChannel;

/**
 * @author wangjiabao
 */
@AllArgsConstructor
public class ReadEventHandler implements Handler, Runnable {

    private final SocketChannel socketChannel;

    @Override
    public void handleEvent(SocketChannel channel) {
        // handle read event
    }

    @Override
    public void run() {
        this.handleEvent(socketChannel);
    }
}
