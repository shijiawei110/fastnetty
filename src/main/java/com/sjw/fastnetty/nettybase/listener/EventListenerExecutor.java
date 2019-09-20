package com.sjw.fastnetty.nettybase.listener;

import lombok.extern.slf4j.Slf4j;

import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.TimeUnit;

/**
 * @author shijiawei
 * @version EventListenerExecutor.java, v 0.1
 * @date 2019/1/22
 * 事件监听线程
 */
@Slf4j
public class EventListenerExecutor implements Runnable{

    private static final int MAX_EVENT_NUM = 4096;

    /**监听标志位**/
    private boolean isStopFlag = false;

    /**监听实现者**/
    private ChannelEventListener listener;

    /**监听事件队列**/
    private LinkedBlockingQueue<ListenEvent> eventQueue = new LinkedBlockingQueue<ListenEvent>();

    public void setStopFlag(boolean stopFlag) {
        isStopFlag = stopFlag;
    }

    public void setListener(ChannelEventListener listener) {
        this.listener = listener;
    }

    public void addEvent(ListenEvent event) {
        if (this.eventQueue.size() <= MAX_EVENT_NUM) {
            this.eventQueue.add(event);
        } else {
            log.error("listen event queue is max, drop this event size={} event={}", eventQueue.size(), event.toString());
        }
    }


    @Override
    public void run(){
        log.info("netty event listen thread start");
        while(!isStopFlag){
            try {
                ListenEvent event = this.eventQueue.poll(2000, TimeUnit.MILLISECONDS);
                if (event != null && listener != null) {
                    switch (event.getType()) {
                        case CLOSE:
                            listener.onChannelClose(event.getLinkAddress(), event.getChannel());
                            break;
                        case CONNECT:
                            listener.onChannelConnect(event.getLinkAddress(), event.getChannel());
                            break;
                        case EXCEPTION:
                            listener.onChannelException(event.getLinkAddress(), event.getChannel());
                            break;
                        default:
                            break;
                    }
                }
            } catch (Exception e) {
                log.error("netty event listen exception -> ", e);
            }
        }
        log.info("netty event listen thread end");
    }
}
