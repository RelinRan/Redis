package socket.redis;

import java.util.List;

/**
 * 订阅
 */
public class Subscribe implements Runnable {

    private Channel channel;
    private OnSubscribeListener onSubscribeListener;
    private boolean start;

    public Subscribe(Channel channel, OnSubscribeListener onSubscribeListener) {
        this.channel = channel;
        this.onSubscribeListener = onSubscribeListener;
        start = true;
    }

    @Override
    public void run() {
        while (start) {
            byte[] data = channel.read();
            Protocol protocol = channel.getProtocol();
            List<byte[]> bytes = protocol.array(data);
            int size = bytes.size();
            if (size >= 3) {
                String type = new String(bytes.get(0)).toUpperCase();
                byte[] channel = bytes.get(1);
                byte[] last = bytes.get(size - 1);
                if (type.equals(Type.SUBSCRIBE.name())) {
                    long count = protocol.toLong(last);
                    if (onSubscribeListener != null) {
                        onSubscribeListener.onSubscribeSuccessful(count);
                    }
                }
                if (type.equals(Type.MESSAGE.name())) {
                    if (onSubscribeListener != null) {
                        onSubscribeListener.onSubscribeReceived(channel, last);
                    }
                }
            }
        }
    }

    public void cancel() {
        start = false;
    }

}
