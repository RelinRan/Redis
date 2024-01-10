package socket.redis;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.ByteBuffer;
import java.nio.channels.SocketChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;

/**
 * 连接通道
 */
public class Channel {

    private SocketChannel channel;
    private String host;
    private int port;
    private ByteBuffer reader;
    private ByteBuffer writer;
    private Charset charset;
    private Protocol protocol;

    /**
     * 连接
     *
     * @param host 主机
     * @param port 端口
     */
    public Channel(String host, int port) {
        this.host = host;
        this.port = port;
        charset = StandardCharsets.UTF_8;
        protocol = new Protocol(charset);
    }

    /**
     * 连接
     */
    public void connect() {
        if (isConnected() || isConnectionPending()) {
            return;
        }
        try {
            channel = SocketChannel.open();
            channel.configureBlocking(true);
            channel.connect(new InetSocketAddress(host, port));
            channel.finishConnect();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 获取协议对象
     *
     * @return
     */
    public Protocol getProtocol() {
        return protocol;
    }

    /**
     * 是否已连接成功
     *
     * @return
     */
    public boolean isConnected() {
        if (channel == null) {
            return false;
        }
        return channel.isConnected();
    }

    /**
     * 是否处于正在连接的状态
     *
     * @return
     */
    public boolean isConnectionPending() {
        if (channel == null) {
            return false;
        }
        return channel.isConnectionPending();
    }

    /**
     * 读取数据
     *
     * @return
     */
    public byte[] read() {
        return read(1024*512);
    }

    /**
     * 读取数据
     *
     * @return
     */
    public byte[] read(int capacity) {
        if (channel != null) {
            reader = ByteBuffer.allocate(capacity);
            try {
                while (channel.read(reader) != -1) {
                    reader.flip();
                    while (reader.hasRemaining()) {
                        byte[] data = new byte[reader.remaining()];
                        reader.get(data);
                        return data;
                    }
                    reader.clear();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return new byte[]{};
    }

    /**
     * 发送数据
     *
     * @param data
     */
    public void send(byte[] data) {
        try {
            writer = ByteBuffer.wrap(data);
            while (writer.hasRemaining()) {
                channel.write(writer);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 发送
     *
     * @param command 指令
     * @param args    参数
     */
    public void send(Command command, String... args) {
        send(protocol.toSending(command, args));
    }

    /**
     * 发送
     *
     * @param command 指令
     * @param args    参数
     */
    public void send(Command command, byte[]... args) {
        send(protocol.toSending(command, args));
    }

    /**
     * 关闭连接
     */
    public void close() {
        try {
            if (reader != null) {
                reader.clear();
                reader = null;
            }
            if (writer != null) {
                writer.clear();
                writer = null;
            }
            if (channel != null) {
                channel.close();
                channel = null;
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
