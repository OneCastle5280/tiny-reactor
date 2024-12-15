package org.wang.tinyreactor.handler;


import lombok.AllArgsConstructor;

import java.nio.channels.SocketChannel;

/**
 * @author wangjiabao
 */
@AllArgsConstructor
public class WriteEventHandler implements Handler, Runnable {

    private final SocketChannel socketChannel;

    @Override
    public void handleEvent(SocketChannel channel) {
        // handle write event
    }

    @Override
    public void run() {
        this.handleEvent(socketChannel);
    }
}
