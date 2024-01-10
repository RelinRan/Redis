package socket.redis;

/**
 * 订阅监听
 */
public interface OnSubscribeListener {

    /**
     * 订阅成功
     * @param count 通道数量
     */
    void onSubscribeSuccessful(Long count);

    /**
     * 订阅接受信息
     *
     * @param channel 通道
     * @param message 消息
     */
    void onSubscribeReceived(byte[] channel, byte[] message);

}
