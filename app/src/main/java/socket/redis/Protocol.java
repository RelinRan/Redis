package socket.redis;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 协议
 */
public class Protocol {
    /**
     * 回车换行符
     */
    public static final String CRLF = "\r\n";
    //*********************[标识]*********************
    /**
     * 单行字符串，格式：+OK\r\n
     */
    public static final byte SIMPLE_STRING = '+';
    /**
     * 简单错误，格式：-Error message\r\n
     */
    public static final byte SIMPLE_ERROR = '-';
    /**
     * 整数，格式：*<number-of-elements>\r\n<element-1>...<element-n>
     */
    public static final byte INTEGER = ':';
    /**
     * 多行字符串，格式：*<number-of-elements>\r\n<element-1>...<element-n>
     */
    public static final byte BULK_STRING = '$';
    /**
     * 数组，格式：*<number-of-elements>\r\n<element-1>...<element-n>
     */
    public static final byte ARRAY = '*';
    /**
     * 空，格式：_\r\n
     */
    public static final byte NULL = '_';
    /**
     * Booleans，格式：#<t|f>\r\n
     */
    public static final byte BOOLEANS = '#';
    /**
     * Doubles，格式：,[<+|->]<integral>[.<fractional>][<E|e>[sign]<exponent>]\r\n
     */
    public static final byte DOUBLE = ',';
    /**
     * Big numbers，格式：([+|-]<number>\r\n
     */
    public static final byte BIG_NUMBERS = '(';
    /**
     * 批量错误，格式：!<length>\r\n<error>\r\n
     */
    public static final byte BULK_ERROR = '!';
    /**
     * 批量字符串，格式：=<length>\r\n<encoding>:<data>\r\n
     */
    public static final byte VERBATIM_STRING = '=';
    /**
     * 键值对，格式：%<number-of-entries>\r\n<key-1><value-1>...<key-n><value-n>
     * For example, the following JSON object:
     * {
     * "first": 1,
     * "second": 2
     * }
     * Can be encoded in RESP like so:
     * %2\r\n
     * +first\r\n
     * :1\r\n
     * +second\r\n
     * :2\r\n
     */
    public static final byte MAP = '%';
    /**
     * 集合，格式：~<number-of-elements>\r\n<element-1>...<element-n>
     */
    public static final byte SET = '~';
    /**
     * RESP的推送包含带外数据。它们是协议的请求-响应模型的例外，并为连接提供通用推送模式。
     * 格式：><number-of-elements>\r\n<element-1>...<element-n>
     */
    public static final byte PUSH = '>';

    private Charset charset;
    private List<byte[]> list;
    private List<String> strings;
    private Map<String, String> map;
    private ByteArrayOutputStream bos;
    private StringBuilder builder;

    public Protocol(Charset charset) {
        this.charset = charset;
    }

    /**
     * 包装发送数据
     *
     * @param command 命令
     * @param args    参数
     * @return
     */
    public byte[] toSending(Command command, String... args) {
        bos = new ByteArrayOutputStream();
        //数组
        write(bos, ARRAY);
        int argsLength = args == null ? 0 : args.length;
        for (int i = 0; i < argsLength; i++) {
            String item = args[i];
            if (item == null || item.length() == 0) {
                argsLength--;
            }
        }
        write(bos, String.valueOf(argsLength + 1).getBytes(charset));
        write(bos, CRLF.getBytes(charset));
        //指令
        write(bos, BULK_STRING);
        write(bos, String.valueOf(command.name().length()).getBytes(charset));
        write(bos, CRLF.getBytes(charset));
        write(bos, command.name().getBytes(charset));
        write(bos, CRLF.getBytes(charset));
        //参数
        for (String arg : args) {
            if (arg == null || arg.length() == 0) {
                continue;
            }
            byte[] bytes = arg.getBytes(charset);
            write(bos, BULK_STRING);
            write(bos, String.valueOf(bytes.length).getBytes(charset));
            write(bos, CRLF.getBytes(charset));
            write(bos, bytes);
            write(bos, CRLF.getBytes(charset));
        }
        byte[] data = bos.toByteArray();
        return data;
    }

    /**
     * 包装发送数据
     *
     * @param command 命令
     * @param args    参数
     * @return
     */
    public byte[] toSending(Command command, byte[]... args) {
        bos = new ByteArrayOutputStream();
        //数组
        write(bos, ARRAY);
        write(bos, toBytes(args == null ? 0 : args.length + 1));
        write(bos, CRLF.getBytes(charset));
        //指令
        write(bos, BULK_STRING);
        write(bos, toBytes(command.name().length()));
        write(bos, CRLF.getBytes(charset));
        write(bos, command.name().getBytes(charset));
        write(bos, CRLF.getBytes(charset));
        //参数
        for (byte[] item : args) {
            write(bos, BULK_STRING);
            write(bos, toBytes(item.length));
            write(bos, CRLF.getBytes(charset));
            write(bos, item);
            write(bos, CRLF.getBytes(charset));
        }
        return bos.toByteArray();
    }

    /**
     * 写入
     *
     * @param bos  字节
     * @param data 字节数组
     */
    private void write(ByteArrayOutputStream bos, byte[] data) {
        try {
            bos.write(data);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * 写入
     *
     * @param bos 字节
     * @param b   字节
     */
    private void write(ByteArrayOutputStream bos, byte b) {
        bos.write(b);
    }

    /**
     * int转字节数组
     *
     * @param value 整数
     * @return
     */
    private byte[] toBytes(int value) {
        return new byte[]{
                (byte) ((value >> 24) & 0xFF),
                (byte) ((value >> 16) & 0xFF),
                (byte) ((value >> 8) & 0xFF),
                (byte) (value & 0xFF)
        };
    }

    /**
     * 内联命令
     *
     * @param command 命令
     * @param args    参数
     * @return
     */
    public byte[] toInline(Command command, String... args) {
        builder = new StringBuilder();
        builder.append(command.name());
        int length = args == null ? 0 : args.length;
        for (int i = 0; i < length; i++) {
            builder.append(" ");
            builder.append(args[i]);
        }
        return builder.toString().getBytes(charset);
    }

    /**
     * 读取单行内容
     *
     * @param data 返回字节
     * @return
     */
    public byte[] simpleString(byte[] data) {
        if (data == null || data.length == 0) {
            return data;
        }
        if (data[0] == SIMPLE_ERROR) {
            return simpleError(data);
        }
        return Arrays.copyOfRange(data, 1, data.length - 2);
    }

    /**
     * 读取简单错误
     *
     * @param data 返回字节
     * @return
     */
    public byte[] simpleError(byte[] data) {
        if (data == null || data.length == 0) {
            return data;
        }
        return Arrays.copyOfRange(data, 1, data.length - 2);
    }

    /**
     * 读取多行内容
     *
     * @param data 返回字节
     * @return
     */
    public byte[] bulkString(byte[] data) {
        int length = data.length;
        if (data == null || length == 0) {
            return data;
        }
        if (data[0] == SIMPLE_ERROR) {
            return simpleError(data);
        }
        if (data[0] == BULK_STRING && data[1] == '-' && data[2] == '1') {
            return "-1".getBytes(charset);
        }
        int index = 0;
        while (index < data.length) {
            if (data[index] == '\r') {
                break;
            }
            index++;
        }
        if (index + 2 >= length || length - 2 < 0) {
            return new byte[]{};
        }
        return Arrays.copyOfRange(data, index + 2, length - 2);
    }

    /**
     * 读取整形
     *
     * @param data
     * @return
     */
    public Long integer(byte[] data) {
        int length = data.length;
        if (data == null || length == 0) {
            return 0L;
        }
        if (data[0] == SIMPLE_ERROR) {
            return 0L;
        }
        if (length - 2 < 0) {
            return 0L;
        }
        return toLong(Arrays.copyOfRange(data, 1, length - 2));
    }

    /**
     * 读取数组
     *
     * @param data 字节数组
     * @return data[0]:type,data[1]:topic,data[0]:message,
     */
    public List<byte[]> array(byte[] data) {
        int from = 0, to = 0;
        list = new ArrayList<>();
        if (data == null || data.length == 0) {
            return list;
        }
        while (to < data.length) {
            if (data[to] == '\r') {
                if (data[from] != ARRAY && data[from] != BULK_STRING) {
                    list.add(Arrays.copyOfRange(data, data[from] == INTEGER ? from + 1 : from, to));
                }
                from = to + 2;
                to++;
            }
            to++;
        }
        return list;
    }

    /**
     * 转字符串集合
     *
     * @param data
     * @return
     */
    public List<String> strings(byte[] data) {
        int from = 0, to = 0;
        strings = new ArrayList<>();
        if (data == null || data.length == 0) {
            return strings;
        }
        while (to < data.length) {
            if (data[to] == '\r') {
                if (data[from] != ARRAY && data[from] != BULK_STRING) {
                    byte[] subs = Arrays.copyOfRange(data, data[from] == INTEGER ? from + 1 : from, to);
                    String content = new String(subs);
                    strings.add(content);
                }
                from = to + 2;
                to++;
            }
            to++;
        }
        if (strings.contains("")) {
            strings = new ArrayList<>();
        }
        return strings;
    }

    /**
     * 转long
     *
     * @param data
     * @return
     */
    public Long toLong(byte[] data) {
        if (data == null || data.length == 0) {
            return 0L;
        }
        return Long.parseLong(new String(data).replace("\"", "").trim());
    }

    /**
     * 转int
     *
     * @param data
     * @return
     */
    public int toInt(byte[] data) {
        if (data == null || data.length == 0) {
            return 0;
        }
        return Integer.parseInt(new String(data).replace("\"", "").trim());
    }

    /**
     * 读取多行内容
     *
     * @param data 返回字节
     * @return
     */
    public Map<String, String> hashMap(byte[] data) {
        int from = 0, to = 0, count = 0;
        map = new HashMap<>();
        if (data == null || data.length == 0) {
            return map;
        }
        if (data[0] == SIMPLE_ERROR) {
            return map;
        }
        String key = null, value = null;
        while (to < data.length) {
            if (data[to] == '\r') {
                if (data[from] != ARRAY && data[from] != BULK_STRING) {
                    count++;
                    byte[] item = Arrays.copyOfRange(data, data[from] == INTEGER ? from + 1 : from, to);
                    if (count % 2 != 0) {
                        key = new String(item);
                    } else {
                        value = new String(item);
                    }
                    if (key != null && value != null) {
                        map.put(key, value);
                        key = value = null;
                    }
                }
                from = to + 2;
                to++;
            }
            to++;
        }
        return map;
    }

}
