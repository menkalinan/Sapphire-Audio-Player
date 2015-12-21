package main.java.com.sdx2.SapphireAudioPlayer.audio;

import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingDeque;

public abstract class PlayerActor {
    public enum Message {
        PLAY, PAUSE, STOP, FLUSH, OPEN, SEEK, VOLUME;

        private Object[] params;
        public Object[] getParams() {
            return params;
        }
        public void setParams(Object[] params) {
            this.params = params;
        }
    }

    private BlockingQueue<Message> queue = new LinkedBlockingDeque<Message>();

    public synchronized void send(Message message, Object... params) {
        message.setParams(params);
        queue.add(message);
    }

    protected PlayerActor() {
        Thread messageThread = new Thread(new Runnable() {
            @Override
            public void run() {
                while (true) {
                    Message message = null;
                    try {
                        message = queue.take();
                        process(message);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                        break;
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }, "PlayerActor Thread");
        messageThread.start();
    }

    protected abstract void process(Message message);
}
