package socket.redis;

import java.io.Closeable;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 远程字典服务器客户端
 */
public class Redis implements Closeable {

    private Channel channel;
    private ConcurrentHashMap<String, Subscribe> subscribeHashMap;
    private ExecutorService subscribeService;
    private Protocol protocol;

    /**
     * 远程字典服务器客户端
     *
     * @param host 主机
     * @param port 端口
     */
    public Redis(String host, int port) {
        subscribeService = Executors.newFixedThreadPool(1);
        subscribeHashMap = new ConcurrentHashMap<>();
        channel = new Channel(host, port);
        channel.connect();
        protocol = channel.getProtocol();
    }

    /**
     * 协议对象
     *
     * @return
     */
    public Protocol protocol() {
        return protocol;
    }

    /**
     * 发送
     *
     * @param data 数据
     */
    public void send(byte[] data) {
        channel.send(data);
    }

    /**
     * 发送信息
     *
     * @param command 指令
     * @param args    参数
     */
    public void send(Command command, String... args) {
        channel.send(command, args);
    }

    /**
     * 发送信息
     *
     * @param command 指令
     * @param args    参数
     */
    public void send(Command command, byte[]... args) {
        channel.send(command, args);
    }

    /**
     * 授权
     *
     * @param password 密码
     */
    public String auth(String password) {
        return auth(null, password);
    }

    /**
     * 授权
     *
     * @param user     用户名
     * @param password 密码
     */
    public String auth(String user, String password) {
        if (user == null) {
            send(Command.AUTH, password);
        } else {
            send(Command.AUTH, user, password);
        }
        return new String(protocol.simpleString(read()));
    }

    /**
     * 读取单个
     *
     * @return
     */
    public byte[] read() {
        if (channel == null) {
            return new byte[]{};
        }
        return channel.read();
    }

    /**
     * 读取单个
     *
     * @param capacity 一次读取的大小
     * @return
     */
    public byte[] read(int capacity) {
        if (channel == null) {
            return new byte[]{};
        }
        return channel.read(capacity);
    }

    /**
     * ping服务器是否可用
     *
     * @param message
     * @return
     */
    public String ping(String message) {
        send(Command.PING, message);
        return new String(protocol.bulkString(read()));
    }

    /**
     * ping服务器是否可用，返回PONG即可用
     *
     * @return
     */
    public String ping() {
        send(Command.PING, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 关闭服务器连接
     *
     * @return
     */
    public String quit() {
        send(Command.QUIT, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 获取值
     *
     * @param key 键
     * @return
     */
    public String get(String key) {
        send(Command.GET, key);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 获取删除key
     *
     * @param key
     * @return
     */
    public String getDel(String key) {
        send(Command.GET, key);
        String value = new String(protocol.bulkString(read()));
        send(Command.DEL, key);
        read();
        return value;
    }

    /**
     * 获取值
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public String set(String key, String value) {
        send(Command.SET, key, value);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 同时设置多个键-值对
     *
     * @param map
     * @return
     */
    public String mset(Map<String, String> map) {
        String[] args = new String[map.size() * 2];
        Iterator<String> iterator = map.keySet().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            args[index] = key;
            index++;
            String value = map.get(key);
            args[index] = value;
            index++;
        }
        send(Command.MSET, args);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于同时设置多个键-值对，但仅在所有指定的键都不存在时才进行设置
     *
     * @param map
     * @return
     */
    public String msetnx(Map<String, String> map) {
        String[] args = new String[map.size() * 2];
        Iterator<String> iterator = map.keySet().iterator();
        int index = 0;
        while (iterator.hasNext()) {
            String key = iterator.next();
            args[index] = key;
            index++;
            String value = map.get(key);
            args[index] = value;
            index++;
        }
        send(Command.MSETNX, args);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 删除指定key
     *
     * @param key
     * @return
     */
    public Long del(String... key) {
        send(Command.DEL, key);
        return protocol.integer(read());
    }

    /**
     * 异步删除指定的键
     *
     * @param key
     * @return
     */
    public Long unlink(String... key) {
        send(Command.UNLINK, key);
        return protocol.integer(read());
    }

    /**
     * 删除当前数据库中的所有键
     *
     * @param async 是否异步
     * @return
     */
    public Long flushDB(boolean async) {
        send(Command.FLUSHDB, async ? "ASYNC" : null);
        return protocol.integer(read());
    }

    /**
     * 删除 Redis 中的所有数据库中的所有键
     *
     * @return
     */
    public String flushAll(boolean async) {
        send(Command.FLUSHALL, async ? "ASYNC" : null);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 随机获取数据库中的一个键
     *
     * @return
     */
    public String randomKey() {
        send(Command.RANDOMKEY, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 重命名一个键
     *
     * @return
     */
    public String rename(String key, String name) {
        send(Command.RENAME, key, name);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 在新的键名不存在时重命名一个键
     *
     * @param key  键
     * @param name 新名称
     * @return
     */
    public String renamenx(String key, String name) {
        send(Command.RENAMENX, key, name);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 查询当前数据库中键的数量
     *
     * @return
     */
    public Long dbSize() {
        send(Command.DBSIZE, new String[]{});
        return protocol.integer(read());
    }

    /**
     * 切换到指定的数据库
     *
     * @return
     */
    public String select(int index) {
        send(Command.SELECT, String.valueOf(index));
        return new String(protocol.simpleString(read()));
    }

    /**
     * 将指定的键从当前数据库移动到另一个数据库
     *
     * @param key   键
     * @param index 数据库下标
     * @return 0:失败，1：成
     */
    public Long move(String key, int index) {
        send(Command.MOVE, key, String.valueOf(index));
        return protocol.integer(read());
    }

    /**
     * 获取指定键存储的值的数据类型
     *
     * @param key
     * @return string：表示值是字符串。
     * list：表示值是列表。
     * set：表示值是集合。
     * zset：表示值是有序集合。
     * hash：表示值是哈希表。
     * stream：表示值是流（Redis 5.0 版本以及更高版本中新增）。
     */
    public String type(String key) {
        send(Command.TYPE, key);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 是否存在key
     *
     * @param keys
     * @return
     */
    public Long exists(String... keys) {
        send(Command.EXISTS, keys);
        return protocol.integer(read());
    }

    /**
     * 根据指定的pattern模式返回匹配的key列表
     *
     * @param pattern 规格
     * @return
     */
    public List<byte[]> keys(String pattern) {
        send(Command.KEYS, pattern);
        return protocol.array(read());
    }

    /**
     * 设置指定key的过期时间（以秒为单位）
     *
     * @param key     键
     * @param seconds 单位秒
     * @return
     */
    public Long expire(String key, int seconds) {
        send(Command.EXPIRE, key, String.valueOf(seconds));
        return protocol.integer(read());
    }

    /**
     * 设置指定key的过期时间
     *
     * @param key       键
     * @param timestamp 时间戳
     * @return
     */
    public Long expireAt(String key, Long timestamp) {
        send(Command.EXPIREAT, key, String.valueOf(timestamp));
        return protocol.integer(read());
    }

    /**
     * 获取指定key的剩余过期时间（以秒为单位）
     *
     * @param key 键
     * @return
     */
    public Long ttl(String key) {
        send(Command.TTL, key);
        return protocol.integer(read());
    }

    /**
     * 获取指定键的值，并将新值设置为该键的值
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public String getSet(String key, String value) {
        send(Command.GETSET, key, value);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 获取多个键的值
     *
     * @param key 键
     * @return
     */
    public List<String> mget(String... key) {
        send(Command.MGET, key);
        return protocol.strings(read());
    }

    /**
     * 设置一个键的值，仅当该键不存在时才进行设置
     *
     * @param key   键
     * @param value 值
     * @return 0失败 1成功
     */
    public Long setnx(String key, String value) {
        send(Command.SETNX, key, value);
        return protocol.integer(read());
    }

    /**
     * 设置一个键-值对，并指定键的过期时间
     *
     * @param key     键
     * @param value   值
     * @param seconds 过期时间,秒为单位
     * @return OK
     */
    public String setex(String key, String value, int seconds) {
        send(Command.SETEX, key, String.valueOf(seconds), value);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 将指定键的值按给定的减量进行减法运算
     *
     * @param key       键
     * @param decrement 要减去的数量
     * @return
     */
    public Long decrby(String key, Long decrement) {
        send(Command.DECRBY, key, String.valueOf(decrement));
        return protocol.integer(read());
    }

    /**
     * 获取递减值
     *
     * @param key 键
     * @return
     */
    public Long decr(String key) {
        send(Command.DECR, key);
        return protocol.integer(read());
    }

    /**
     * 将指定键的值按给定的增量进行加法运算
     *
     * @param key       键
     * @param decrement 要减去的数量
     * @return
     */
    public Long incrby(String key, Long decrement) {
        send(Command.INCRBY, key, String.valueOf(decrement));
        return protocol.integer(read());
    }

    /**
     * 获取递增值
     *
     * @param key 键
     * @return
     */
    public Long incr(String key) {
        send(Command.INCR, key);
        return protocol.integer(read());
    }

    /**
     * 指定键的值追加一个字符串
     *
     * @param key   键
     * @param value 字符串
     * @return 返回值是追加后的字符串的长度
     */
    public Long append(String key, String value) {
        send(Command.APPEND, key, value);
        return protocol.integer(read());
    }

    /**
     * Redis 1.0 版本中的一个字符串命令，用于获取指定字符串的子串
     *
     * @param key   键
     * @param start 开始下标
     * @param end   结束下标
     * @return
     */
    public String substr(String key, int start, int end) {
        send(Command.SUBSTR, key, String.valueOf(start), String.valueOf(end));
        return new String(protocol.bulkString(read()));
    }

    /**
     * Redis 2.0 版本中的一个字符串命令，用于获取指定字符串的子串
     *
     * @param key   键
     * @param start 开始下标
     * @param end   结束下标
     * @return
     */
    public String getRange(String key, int start, int end) {
        send(Command.GETRANGE, key, String.valueOf(start), String.valueOf(end));
        return new String(protocol.bulkString(read()));
    }

    /**
     * Redis 2.0 将给定字段和值存储在指定的哈希表中
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     * @return 如果成功地设置了字段的值，HSET 命令将返回 1；如果字段已经存在并且新值与旧值相同，那么 HSET 命令将返回 0。
     */
    public Long hset(String key, String field, String value) {
        send(Command.HSET, key, field, value);
        return protocol.integer(read());
    }

    /**
     * 获取指定key的哈希表中字段的值
     *
     * @param key   键
     * @param field 字段
     * @return
     */
    public String hget(String key, String field) {
        send(Command.HGET, key, field);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 只在给定字段不存在时，将字段及其值存储在指定的哈希表中
     *
     * @param key   键
     * @param field 字段
     * @param value 值
     * @return 成功地设置了字段的值，HSETNX 命令将返回 1；如果字段已经存在并且新值与旧值相同，那么 HSETNX 命令将返回 0。
     */
    public Long hsetnx(String key, String field, String value) {
        send(Command.HSETNX, key, field, value);
        return protocol.integer(read());
    }

    /**
     * 将多个字段和值同时设置到指定key的哈希表中
     *
     * @param key 键
     * @param map 值
     * @return
     */
    public String hmset(String key, Map<String, String> map) {
        int size = map.size() * 2 + 1;
        String[] args = new String[size];
        int index = 0;
        args[index] = key;
        for (String mapKey : map.keySet()) {
            index++;
            args[index] = mapKey;
            index++;
            args[index] = map.get(mapKey);
        }
        send(Command.HMSET, args);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 从指定的哈希表中获取多个字段的值
     *
     * @param key   键
     * @param field 字段
     * @return
     */
    public Map<String, String> hmget(String key, String... field) {
        String[] args = new String[field.length + 1];
        args[0] = key;
        for (int i = 0; i < field.length; i++) {
            args[i + 1] = field[i];
        }
        send(Command.HMGET, args);
        List<byte[]> data = protocol.array(read());
        Map<String, String> map = new HashMap<>();
        for (int i = 0; i < field.length; i++) {
            String value = new String(data.get(i));
            map.put(field[i], value);
        }
        return map;
    }

    /**
     * Redis 的一个哈希（Hash）命令，用于将指定字段的值按给定增量递增
     *
     * @param key       键
     * @param field     字段
     * @param increment 增量
     * @return 递增后的值
     */
    public Long hincrby(String key, String field, int increment) {
        send(Command.HINCRBY, key, field, String.valueOf(increment));
        return protocol.integer(read());
    }

    /**
     * 检查指定的哈希表中是否存在指定的字段
     *
     * @param key   键
     * @param field 字段
     * @return 如果指定的哈希表不存在，或者指定的字段不存在于哈希表中，那么 HEXISTS 命令将返回 0。如果指定的字段存在于哈希表中，那么 HEXISTS 命令将返回 1。
     */
    public Long hexists(String key, String field) {
        send(Command.HEXISTS, key, field);
        return protocol.integer(read());
    }

    /**
     * 用于删除指定哈希表中一个或多个字段的值,
     * 如果指定的哈希表、或者指定的字段不存在于哈希表中，那么 HDEL 命令将返回 0。
     * 如果成功删除了指定的字段，则 HDEL 命令将返回实际删除的字段数量（不包括不存在于哈希表中的字段）。
     *
     * @param key   键
     * @param field 字段
     * @return
     */
    public Long hdel(String key, String... field) {
        String[] args = new String[field.length + 1];
        args[0] = key;
        for (int i = 0; i < field.length; i++) {
            args[i + 1] = field[i];
        }
        send(Command.HDEL, args);
        return protocol.integer(read());
    }

    /**
     * 获取指定哈希表中字段的数量
     *
     * @param key 键
     * @return
     */
    public Long hlen(String key) {
        send(Command.HLEN, key);
        return protocol.integer(read());
    }

    /**
     * 获取指定哈希表中所有的字段名
     *
     * @param key 键
     * @return
     */
    public List<String> hkeys(String key) {
        send(Command.HKEYS, key);
        return protocol.strings(read());
    }

    /**
     * 获取指定哈希表中所有的字段值
     *
     * @param key 键
     * @return
     */
    public List<String> hvals(String key) {
        send(Command.HVALS, key);
        return protocol.strings(read());
    }

    /**
     * 获取指定key的哈希表中所有的字段和值
     *
     * @param key 键
     * @return
     */
    public Map<String, String> hgetAll(String key) {
        send(Command.HGETALL, key);
        return protocol.hashMap(read());
    }

    /**
     * Redis 6.2 版本及以上可用,从指定的哈希表中随机返回一个字段以及它的值
     *
     * @param key   键名
     * @param count 指定返回的字段数量
     * @return
     */
    public Map<String, String> hRandField(String key, int count) {
        send(Command.HRANDFIELD, key, String.valueOf(count));
        return protocol.hashMap(read());
    }

    /**
     * 将一个或多个值插入到已存在的列表的尾部
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public Long rpush(String key, String value) {
        send(Command.RPUSH, key, value);
        return protocol.integer(read());
    }

    /**
     * 将一个或多个值插入到列表的头部
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public Long lpush(String key, String value) {
        send(Command.LPUSH, key, value);
        return protocol.integer(read());
    }

    /**
     * 将一个或多个值插入到已存在的列表的尾部。如果指定的key不存在，则该命令不会执行任何操作。
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public Long rpushx(String key, String value) {
        send(Command.RPUSHX, key, value);
        return protocol.integer(read());
    }

    /**
     * 获取指定列表的长度（即包含的元素个数）
     *
     * @param key 键
     * @return
     */
    public Long llen(String key) {
        send(Command.LLEN, key);
        return protocol.integer(read());
    }

    /**
     * 获取指定列表中指定范围内的元素
     *
     * @param key   键
     * @param start 开始索引
     * @param stop  结束索引
     * @return
     */
    public List<String> lrange(String key, int start, int stop) {
        send(Command.LRANGE, key, String.valueOf(start), String.valueOf(stop));
        return protocol.strings(read());
    }

    /**
     * Redis 的一个列表（List）命令，修剪（trim）指定列表，只保留列表中指定范围内的元素，而移除其他元素
     *
     * @param key   键
     * @param start 开始索引
     * @param stop  结束索引
     * @return
     */
    public String ltrim(String key, int start, int stop) {
        send(Command.LTRIM, key, String.valueOf(start), String.valueOf(stop));
        return new String(protocol.simpleString(read()));
    }

    /**
     * 通过索引获取列表中的元素，列表的起始索引是 0。如果索引为正数，返回列表中从左向右数的第 index 个元素；如果索引为负数，返回列表中从右向左数的第 -index 个元素
     *
     * @param key   键
     * @param index 引获
     * @return
     */
    public String lindex(String key, int index) {
        send(Command.LINDEX, key, String.valueOf(index));
        return new String(protocol.simpleString(read()));
    }

    /**
     * 设置列表指定索引处的元素的值
     *
     * @param key   键
     * @param index 引获
     * @param value 值
     * @return
     */
    public String lset(String key, int index, String value) {
        send(Command.LSET, key, String.valueOf(index), value);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 从列表中移除指定数量的元素
     *
     * @param key   键
     * @param count 参数 count 的取值可以为：
     *              <p>
     *              count > 0：从头部开始删除 count 个值为 value 的元素。
     *              count < 0：从尾部开始删除 count 绝对值个值为 value 的元素。
     *              count = 0：删除所有值为 value 的元素。
     * @param value
     * @return LREM 命令返回被移除的元素个数。如果列表中不存在该元素，返回 0。
     */
    public Long lrem(String key, int count, String value) {
        send(Command.LREM, key, String.valueOf(count), value);
        return protocol.integer(read());
    }

    /**
     * 移除并返回列表的头部元素
     *
     * @param key 键
     * @return
     */
    public String lpop(String key) {
        send(Command.LPOP, key);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 移除并返回列表的尾部元素
     *
     * @param key 键
     * @return
     */
    public String rpop(String key) {
        send(Command.RPOP, key);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 将列表 source 中的最后一个元素弹出，并将其插入到列表 destination 的最前面，
     * 形成一个元素从 source 到 destination 的“原子性”操作。如果两个列表不存在，RPOPLPUSH 命令会将返回 nil。
     *
     * @param source      列表
     * @param destination 列表
     * @return
     */
    public String rpoplpush(String source, String destination) {
        send(Command.RPOPLPUSH, source, destination);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 向集合中添加一个或多个元素
     *
     * @param key    键
     * @param member 元素
     * @return
     */
    public Long sadd(String key, String... member) {
        String[] data = new String[member.length + 1];
        data[0] = key;
        for (int i = 0; i < member.length; i++) {
            data[i + 1] = member[i];
        }
        send(Command.SADD, data);
        return protocol.integer(read());
    }

    /**
     * 用于返回集合中的所有元素
     *
     * @param key 键
     * @return
     */
    public List<String> smembers(String key) {
        send(Command.SMEMBERS, key);
        return protocol.strings(read());
    }

    /**
     * 从集合中删除一个或多个元素
     *
     * @param key    键
     * @param member 元素
     * @return SREM 命令只会删除集合中存在的元素。如果删除的元素不在集合中，
     * SREM 命令会被忽略，返回删除的元素个数为0。如果指定的集合不存在，
     * 则什么也不会发生，返回删除的元素个数为0。
     */
    public Long srme(String key, String... member) {
        String[] data = new String[member.length + 1];
        data[0] = key;
        for (int i = 0; i < member.length; i++) {
            data[i + 1] = member[i];
        }
        send(Command.SREM, data);
        return protocol.integer(read());
    }

    /***
     *从集合中随机地移除并返回一个或多个元素
     * @param key 键
     * @param count 个数
     * @return
     */
    public List<String> spop(String key, int count) {
        send(Command.SPOP, key, String.valueOf(count));
        return protocol.strings(read());
    }

    /**
     * 将集合中的一个元素移动到另一个集合中
     *
     * @param source      源集合的键名
     * @param destination 目标集合的键名
     * @param member      要移动的元素
     * @return 如果源集合中不存在指定的元素，则 SMOVE 命令不会进行任何操作，返回0。如果目标集合中已经存在指定的元素，
     * 则 SMOVE 命令会将元素从源集合中删除。如果指定的源集合或目标集合不存在，则 SMOVE 命令不会进行任何操作，返回0。
     */
    public Long smove(String source, String destination, String member) {
        send(Command.SMOVE, source, destination, member);
        return protocol.integer(read());
    }

    /**
     * 获取集合中的成员数量
     *
     * @param key 键
     * @return
     */
    public Long scard(String key) {
        send(Command.SCARD, key);
        return protocol.integer(read());
    }

    /**
     * 判断一个元素是否属于集合
     *
     * @param key    键
     * @param member 元素
     * @return
     */
    public boolean sismember(String key, String member) {
        send(Command.SISMEMBER, key, member);
        return protocol.integer(read()) == 1;
    }

    /**
     * 用于求两个或多个集合的交集
     *
     * @param key 键
     * @return
     */
    public List<String> sinter(String... key) {
        send(Command.SINTER, key);
        return protocol.strings(read());
    }

    /**
     * 用于求两个或多个集合的交集，并将结果存储到一个新的集合中
     *
     * @param destination 存储交集结果的新集合的键名
     * @param key         要求交集的集合的键名
     * @return
     */
    public Long sinterstore(String destination, String... key) {
        String[] data = new String[key.length + 1];
        data[0] = destination;
        for (int i = 0; i < key.length; i++) {
            data[i + 1] = key[i];
        }
        send(Command.SINTERSTORE, data);
        return protocol.integer(read());
    }

    /**
     * 用于求两个或多个集合的并集
     *
     * @param key 要求并集的集合的键名
     * @return
     */
    public List<String> sunion(String... key) {
        send(Command.SUNION, key);
        return protocol.strings(read());
    }

    /**
     * 用于求两个或多个集合的并集，并将结果存储到一个新的集合中
     *
     * @param destination 存储并集结果的新集合的键名
     * @param key         要求并集的集合的键名
     * @return
     */
    public Long sunionstore(String destination, String... key) {
        String[] data = new String[key.length + 1];
        data[0] = destination;
        for (int i = 0; i < key.length; i++) {
            data[i + 1] = key[i];
        }
        send(Command.SUNIONSTORE, key);
        return protocol.integer(read());
    }

    /**
     * 用于求两个集合的差集，即从第一个集合中移除在其他集合中存在的元素
     *
     * @param key 要求差集的集合的键名
     * @return
     */
    public List<String> sdiff(String... key) {
        send(Command.SDIFF, key);
        return protocol.strings(read());
    }

    /**
     * 向有序集合（sorted set）中添加一个或多个成员的命令
     *
     * @param key   键
     * @param value 值 [score,value]
     * @return
     */
    public Long zadd(String key, Map<Integer, String> value) {
        String[] args = new String[value.size() * 2 + 1];
        args[0] = key;
        int index = 1;
        for (Integer mapKey : value.keySet()) {
            args[index] = mapKey + "";
            index++;
            args[index] = value.get(mapKey);
            index++;
        }
        send(Command.ZADD, args);
        return protocol.integer(read());
    }

    /**
     * 获取有序集合中的一定范围内的成员
     *
     * @param key   有序集合键名
     * @param start 起始位置，以 0 表示有序集合中的第一个元素
     * @param stop  终止位置，以 -1 表示有序集合中的最后一个元素。
     * @return
     */
    public List<String> zrange(String key, int start, int stop) {
        send(Command.ZRANGE, key, start + "", stop + "");
        return protocol.strings(read());
    }

    /**
     * 从有序集合中移除一个或多个指定的成员
     *
     * @param key    有序集合键名
     * @param member 要移除的成员
     * @return 删除成员个数
     */
    public Long zrem(String key, String... member) {
        String[] args = new String[member.length + 1];
        args[0] = key;
        for (int i = 0; i < member.length; i++) {
            args[i + 1] = member[i];
        }
        send(Command.ZREM, args);
        return protocol.integer(read());
    }

    /**
     * 对有序集合中指定成员的分值进行增加或减少操作
     *
     * @param key       有序集合的键名
     * @param increment 要增加或减少的分值。可以是正数或负数
     * @param member    要操作分值的成员
     * @return 当前分值
     */
    public String zincrby(String key, int increment, String member) {
        send(Command.ZINCRBY, key, increment + "", member);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 获取有序集合中指定成员的排名（即索引位置）
     *
     * @param key    有序集合的键名
     * @param member 要获取排名的成员
     * @return
     */
    public Long zrank(String key, String member) {
        send(Command.ZRANK, key, member);
        return protocol.integer(read());
    }

    /**
     * 获取有序集合中指定成员的逆序排名（即从大到小的排名）。
     *
     * @param key    有序集合的键名
     * @param member 要获取排名的成员
     * @return
     */
    public Long zrevrank(String key, String member) {
        send(Command.ZREVRANK, key, member);
        return protocol.integer(read());
    }


    /**
     * 按照逆序获取有序集合中指定排名范围内的成员
     *
     * @param key   有序集合键名
     * @param start 起始位置，以 0 表示有序集合中的第一个元素
     * @param stop  终止位置，以 -1 表示有序集合中的最后一个元素。
     * @return
     */
    public List<String> zrevrange(String key, int start, int stop) {
        send(Command.ZREVRANGE, key, start + "", stop + "");
        return protocol.strings(read());
    }

    /**
     * 获取有序集合中成员的数量
     *
     * @param key 有序集合键名
     * @return
     */
    public Long zcard(String key) {
        send(Command.ZCARD, key);
        return protocol.integer(read());
    }

    /**
     * 获取有序集合中指定成员的分值
     *
     * @param key    有序集合键名
     * @param member 要获取分值的成员
     * @return
     */
    public String zscore(String key, String member) {
        send(Command.ZSCORE, key, member);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 删除并获取有序集合中分值最大的成员
     *
     * @param key   有序集合的键名
     * @param count 可选参数，表示一次性获取的成员数量。如果不指定此参数，则默认获取一个成员
     * @return
     */
    public List<String> zpopmax(String key, int count) {
        send(Command.ZPOPMAX, key, count + "");
        return protocol.strings(read());
    }

    /**
     * 删除并获取有序集合中分值最小的成员
     *
     * @param key   有序集合的键名
     * @param count 可选参数，表示一次性获取的成员数量。如果不指定此参数，则默认获取一个成员
     * @return
     */
    public List<String> zpopmin(String key, int count) {
        send(Command.ZPOPMIN, key, count + "");
        return protocol.strings(read());
    }

    /**
     * 对列表、集合或有序集合中的元素进行排序
     *
     * @param key 需要排序的键
     * @return
     */
    public String sort(String key) {
        send(Command.SORT, key);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 从一个或多个列表的最左端阻塞式地获取并删除第一个元素
     *
     * @param key     要操作的列表键
     * @param timeout 是阻塞超时时间，单位为秒
     * @return
     */
    public String blpop(String[] key, int timeout) {
        String[] args = new String[key.length + 1];
        for (int i = 0; i < key.length; i++) {
            args[i] = key[i];
        }
        args[args.length - 1] = timeout + "";

        send(Command.BLPOP, key);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 从一个或多个列表中阻塞式地获取并删除最后一个元素
     *
     * @param key     要操作的列表键
     * @param timeout 是阻塞超时时间，单位为秒
     * @return
     */
    public String brpop(String[] key, int timeout) {
        String[] args = new String[key.length + 1];
        for (int i = 0; i < key.length; i++) {
            args[i] = key[i];
        }
        args[args.length - 1] = timeout + "";

        send(Command.BRPOP, key);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 对列表、集合或有序集合中的元素进行排序
     *
     * @param key     需要排序的键
     * @param pattern 匹配条件，例如：BY myzset_* DESC 或者GET myset_*
     * @return
     */
    public String sort(String key, String pattern) {
        send(Command.SORT, key, pattern);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 用于求两个或多个集合的差集，并将结果存储到一个新的集合中
     *
     * @param destination 存储差集结果的新集合的键名
     * @param key         要求差集的集合的键名
     * @return
     */
    public Long sdiffstore(String destination, String... key) {
        String[] data = new String[key.length + 1];
        data[0] = destination;
        for (int i = 0; i < key.length; i++) {
            data[i + 1] = key[i];
        }
        send(Command.SDIFFSTORE, data);
        return protocol.integer(read());
    }

    /**
     * 用于从集合中随机返回一个元素
     *
     * @param key   要返回元素的集合的键名
     * @param count 用于指定要返回的元素数量
     * @return
     */
    public List<String> srandmember(String key, int count) {
        send(Command.SRANDMEMBER, key, String.valueOf(count));
        return protocol.strings(read());
    }

    /**
     * 将一个或多个值插入到已存在的列表的尾部
     *
     * @param key    键
     * @param values 多个值
     * @return
     */
    public Long rpush(String key, String... values) {
        String[] data = new String[values.length + 1];
        data[0] = key;
        for (int i = 0; i < values.length; i++) {
            data[i + 1] = values[i];
        }
        send(Command.RPUSH, data);
        return protocol.integer(read());
    }

    /**
     * 订阅
     *
     * @param channel 通道
     * @return
     */
    public void subscribe(String channel, OnSubscribeListener onSubscribeListener) {
        subscribe(new String[]{channel}, onSubscribeListener);
    }

    /**
     * 订阅
     *
     * @param channels 通道数组
     * @return
     */
    public void subscribe(String[] channels, OnSubscribeListener onSubscribeListener) {
        if (channels == null) {
            return;
        }
        send(Command.SUBSCRIBE, channels);
        Subscribe subscribe = new Subscribe(channel, onSubscribeListener);
        subscribeService.submit(subscribe);
        for (int i = 0; i < channels.length; i++) {
            subscribeHashMap.put(channels[i], subscribe);
        }
    }

    /**
     * 订阅一个或多个与给定模式匹配的频道的命令
     *
     * @param patterns            一个或多个用于匹配频道名的模式
     * @param onSubscribeListener
     */
    public void psubscribe(String[] patterns, OnSubscribeListener onSubscribeListener) {
        if (patterns == null) {
            return;
        }
        send(Command.PSUBSCRIBE, patterns);
        Subscribe subscribe = new Subscribe(channel, onSubscribeListener);
        subscribeService.submit(subscribe);
        for (int i = 0; i < patterns.length; i++) {
            subscribeHashMap.put(patterns[i], subscribe);
        }
    }

    /**
     * 用于取消订阅指定模式的频道的命令
     *
     * @param patterns            一个或多个用于匹配频道名的模式
     * @param onSubscribeListener
     */
    public void punsubscribe(String[] patterns, OnSubscribeListener onSubscribeListener) {
        if (patterns == null) {
            return;
        }
        send(Command.PUNSUBSCRIBE, patterns);
        Subscribe subscribe = new Subscribe(channel, onSubscribeListener);
        subscribeService.submit(subscribe);
        for (int i = 0; i < patterns.length; i++) {
            subscribeHashMap.put(patterns[i], subscribe);
        }
    }

    /**
     * 取消所有订阅
     *
     * @return
     */
    public String unsubscribe() {
        Iterator<Map.Entry<String, Subscribe>> iterator = subscribeHashMap.entrySet().iterator();
        while (iterator.hasNext()) {
            Map.Entry<String, Subscribe> entry = iterator.next();
            String key = entry.getKey();
            Subscribe value = entry.getValue();
            value.cancel();
            send(Command.UNSUBSCRIBE, key);
            iterator.remove();
        }
        if (subscribeService != null) {
            subscribeService.shutdown();
        }
        return new String(read());
    }

    /**
     * 取消订阅
     *
     * @param channel 通道
     * @return
     */
    public String unsubscribe(String channel) {
        return unsubscribe(new String[]{channel});
    }

    /**
     * 取消订阅
     *
     * @param channels 通道
     * @return
     */
    public String unsubscribe(String[] channels) {
        send(Command.UNSUBSCRIBE, channels);
        for (String key : channels) {
            if (subscribeHashMap.containsKey(key)) {
                subscribeHashMap.get(key).cancel();
                subscribeHashMap.remove(key);
            }
        }
        if (subscribeService != null) {
            subscribeService.shutdown();
        }
        return new String(read());
    }

    /**
     * 列出当前被订阅的频道
     *
     * @param pattern
     * @return
     */
    public List<String> channels(String pattern) {
        send(Command.PUBSUB, "CHANNELS", pattern);
        return protocol.strings(read());
    }

    /**
     * 获取指定频道的订阅者数量
     *
     * @param channel
     * @return
     */
    public Map<String, String> numsub(String channel) {
        send(Command.PUBSUB, "NUMSUB", channel);
        return protocol.hashMap(read());
    }

    /**
     * 获取被订阅到的模式的数量
     *
     * @return
     */
    public Long numpat() {
        send(Command.PUBSUB, "NUMPAT");
        return protocol.integer(read());
    }

    /**
     * 获取有序集合（Sorted Set）中指定分数范围内的成员数量的命令
     *
     * @param key 有序集合的键名
     * @param min 分数的最小范围
     * @param max 分数的最大范围
     * @return
     */
    public Long zcount(String key, int min, int max) {
        send(Command.ZCOUNT, key, min + "", max + "");
        return protocol.integer(read());
    }

    /**
     * 根据分数范围获取有序集合（Sorted Set）中的成员的命令
     *
     * @param key 有序集合的键名
     * @param min 分数的最小范围
     * @param max 分数的最大范围
     * @return
     */
    public List<String> zrangebyscore(String key, int min, int max) {
        send(Command.ZRANGEBYSCORE, key, min + "", max + "");
        return protocol.strings(read());
    }

    /**
     * 根据分数范围从大到小获取有序集合（Sorted Set）中的成员的命令
     *
     * @param key 有序集合的键名
     * @param max 分数最大值
     * @param min 分数最小值
     * @return
     */
    public List<String> zrevrangebyscore(String key, int max, int min) {
        send(Command.ZREVRANGEBYSCORE, key, max + "", min + "");
        return protocol.strings(read());
    }

    /**
     * 根据排名范围删除有序集合（Sorted Set）中的成员的命令
     *
     * @param key   有序集合的键名
     * @param start 最小范围
     * @param stop  最大范围
     * @return
     */
    public Long zremrangebyrank(String key, int start, int stop) {
        send(Command.ZREMRANGEBYRANK, key, String.valueOf(start), String.valueOf(stop));
        return protocol.integer(read());
    }

    /**
     * 根据分数范围删除有序集合（Sorted Set）中的成员的命令
     *
     * @param key 有序集合的键名
     * @param min 分数最小值
     * @param max 分数最大值
     * @return
     */
    public Long zremrangebyscore(String key, int min, int max) {
        send(Command.ZREMRANGEBYSCORE, key, String.valueOf(min), String.valueOf(max));
        return protocol.integer(read());
    }

    /**
     * 计算多个有序集合的并集，并将结果存储在一个新的有序集合中
     *
     * @param destination 指定存储计算结果的新有序集合的键名
     * @param numkeys     指定要参与并集计算的有序集合数量
     * @param key         要参与并集计算的有序集合的键名
     * @param weights     可选参数，用于指定每个输入有序集合对结果的影响权重，默认权重为 1
     * @param aggregate   可选参数，指定计算并集时的聚合方式，默认为 SUM ,SUM|MIN|MAX
     * @return
     */
    public Long zunionstore(String destination, int numkeys, String[] key, int[] weights, String aggregate) {
        String[] array = new String[key.length + weights.length + 3 + (isEmpty(aggregate) ? -1 : 0)];
        array[0] = destination;
        array[1] = String.valueOf(numkeys);
        int keyLength = key == null ? 0 : key.length;
        for (int i = 0; i < keyLength; i++) {
            array[2 + i] = key[i];
        }
        int weightsLength = weights == null ? 0 : weights.length;
        for (int i = 0; i < weightsLength; i++) {
            array[2 + keyLength + i] = String.valueOf(weights[i]);
        }
        if (!isEmpty(aggregate)) {
            array[array.length - 1] = aggregate;
        }
        send(Command.ZUNIONSTORE, array);
        return protocol.integer(read());
    }

    /**
     * 用于计算多个有序集合的交集，并将结果存储在一个新的有序集合中的命令
     *
     * @param destination 指定存储计算结果的新有序集合的键名
     * @param numkeys     指定要参与并集计算的有序集合数量
     * @param key         要参与并集计算的有序集合的键名
     * @param weights     可选参数，用于指定每个输入有序集合对结果的影响权重，默认权重为 1
     * @param aggregate   可选参数，指定计算并集时的聚合方式，默认为 SUM ,SUM|MIN|MAX
     * @return
     */
    public Long zinterstore(String destination, int numkeys, String[] key, int[] weights, String aggregate) {
        String[] array = new String[key.length + weights.length + 3 + (isEmpty(aggregate) ? -1 : 0)];
        array[0] = destination;
        array[1] = String.valueOf(numkeys);
        int keyLength = key == null ? 0 : key.length;
        for (int i = 0; i < keyLength; i++) {
            array[2 + i] = key[i];
        }
        int weightsLength = weights == null ? 0 : weights.length;
        for (int i = 0; i < weightsLength; i++) {
            array[2 + keyLength + i] = String.valueOf(weights[i]);
        }
        if (!isEmpty(aggregate)) {
            array[array.length - 1] = aggregate;
        }
        send(Command.ZINTERSTORE, array);
        return protocol.integer(read());
    }

    /**
     * 计算有序集合中指定字典区间范围内的成员数量
     *
     * @param key 有序集合的键名
     * @param min 字典区间的起始值,[banana
     * @param max 字典区间的结束值,[grape
     * @return
     */
    public Long zlexcount(String key, String min, String max) {
        send(Command.ZLEXCOUNT, key, min, max);
        return protocol.integer(read());
    }

    /**
     * 获取有序集合中指定字典区间范围内的成员列表
     *
     * @param key 有序集合的键名
     * @param min 字典区间的起始值。可以使用 - 和 + 表示最小值和最大值,"[b"
     * @param max 字典区间的结束值。可以使用 - 和 + 表示最小值和最大值,"[c"
     * @return
     */
    public List<String> zrangebylex(String key, String min, String max) {
        send(Command.ZRANGEBYLEX, key, min, max);
        return protocol.strings(read());
    }

    /**
     * 获取有序集合中指定字典区间范围内的成员列表，但是按照逆序排列返回。
     *
     * @param key 有序集合的键名
     * @param max 字典区间的结束值。可以使用 - 和 + 表示最小值和最大值,"[c"
     * @param min 字典区间的起始值。可以使用 - 和 + 表示最小值和最大值,"[b"
     * @return
     */
    public List<String> zrevrangebylex(String key, String max, String min) {
        send(Command.ZREVRANGEBYLEX, key, max, min);
        return protocol.strings(read());
    }

    /**
     * 移除有序集合（sorted set）中按字典区间（lexicographical range）排列的成员。该命令可以根据指定的字典区间删除有序集合中的成员
     *
     * @param key 有序集合的键名
     * @param min 字典区间的起始值。可以使用 - 和 + 表示最小值和最大值,"[b"
     * @param max 字典区间的结束值。可以使用 - 和 + 表示最小值和最大值,"[c"
     * @return
     */
    public List<String> zremrangebylex(String key, String min, String max) {
        send(Command.ZREMRANGEBYLEX, key, min, max);
        return protocol.strings(read());
    }

    /**
     * 将当前数据库的数据保存到硬盘上的 RDB 文件中
     *
     * @return
     */
    public String save() {
        send(Command.SAVE, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 在后台异步保存当前数据库的数据到硬盘上的 RDB 文件中
     *
     * @return
     */
    public String bgsave() {
        send(Command.BGSAVE, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于在后台异步重写当前的 AOF（Append Only File）文件，它会生成一个新的AOF文件来替代当前的AOF文件。
     * AOF文件是用来记录服务器的每个写操作命令的，通过重新生成AOF文件来达到压缩和优化AOF文件的目的。
     *
     * @return
     */
    public String bgrewriteaof() {
        send(Command.BGREWRITEAOF, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 获取最近一次成功持久化数据库的时间戳。它返回一个表示最后持久化时间的整数值，表示自UNIX纪元（1970年1月1日）以来的秒数。
     *
     * @return
     */
    public Long lastsave() {
        send(Command.LASTSAVE, new String[]{});
        return protocol.integer(read());
    }

    /**
     * 关闭当前运行的 Redis 服务器
     *
     * @return
     */
    public String shutdown() {
        send(Command.SHUTDOWN, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于获取关于 Redis 服务器的各种信息和统计数据。
     * 执行 INFO 命令会返回一个包含多个键值对的字符串，其中包括服务器的各种指标、配置选项、客户端连接信息、内存使用情况、持久化相关信息等
     *
     * @return
     */
    public String info() {
        send(Command.INFO, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 将当前 Redis 服务器设置为一个指定主服务器的从服务器（slave）
     *
     * @param masterip   主服务器的 IP,192.168.1.100
     * @param masterport 端口号 6379
     * @return
     */
    public String slaveof(String masterip, int masterport) {
        send(Command.SLAVEOF, masterip, String.valueOf(masterport));
        return new String(protocol.simpleString(read()));
    }

    /**
     * 将当前 Redis 服务器从从服务器恢复为独立服务器
     *
     * @return
     */
    public String slaveofnoone() {
        send(Command.SLAVEOF, "");
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于实时监视客户端发送到 Redis 服务器的命令请求，并将这些请求实时输出到服务器的日志中。这个命令对于调试和故障排除非常有用。
     *
     * @return
     */
    public String monitor() {
        send(Command.MONITOR, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * CONFIG 命令用于配置 Redis 服务器的运行时参数
     *
     * @param options 获取指定配置选项的值，例如： GET option_name
     *                修改配置选项的值，例如：SET option_name value
     *                要获取最大内存限制配置的值:GET maxmemory
     *                要修改最大内存限制配置的值为 1GB:SET maxmemory 1GB
     *                需要注意的是，并非所有的配置选项都可以通过 CONFIG 命令来修改，
     *                一些选项可能只能在启动时通过配置文件或命令行参数来设置。执行 CONFIG 命令时，
     *                请注意只修改合适的配置选项，以避免意外对服务器造成影响。
     * @return
     */
    public String config(String... options) {
        send(Command.CONFIG, options);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 发布信息
     *
     * @param channel 通道
     * @param message 信息
     * @return 2:成功
     */
    public Long publish(String channel, String message) {
        send(Command.PUBLISH, channel, message);
        return protocol.integer(read());
    }

    /**
     * 用于将一个或多个值插入到已存在的列表的头部。如果指定的 key 不存在，则该命令不会执行任何操作。
     *
     * @param key   键
     * @param value 值
     * @return
     */
    public Long lpushx(String key, String value) {
        send(Command.LPUSHX, key, value);
        return protocol.integer(read());
    }


    /**
     * 将一个或多个值插入到列表的头部
     *
     * @param key    键
     * @param values 多个值
     * @return
     */
    public Long lpush(String key, String... values) {
        String[] data = new String[values.length + 1];
        data[0] = key;
        for (int i = 0; i < values.length; i++) {
            data[i + 1] = values[i];
        }
        send(Command.LPUSH, data);
        return protocol.integer(read());
    }

    /**
     * 获取存储在指定键（key）的字符串值的长度
     *
     * @param key
     * @return
     */
    public Long strlen(String key) {
        send(Command.STRLEN, key);
        return protocol.integer(read());
    }

    /**
     * 用于移除给定键（key）的过期时间，使其成为永久有效的键
     *
     * @param key 要操作的键名称
     * @return
     */
    public Long persist(String key) {
        send(Command.PERSIST, key);
        return protocol.integer(read());
    }

    /**
     * ECHO 命令用于返回输入的参数。它可以用于测试连接是否正常以及验证指令的正确性
     *
     * @param message
     * @return
     */
    public String echo(String message) {
        send(Command.ECHO, message);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 在列表（list）中指定元素的前后位置插入一个新元素
     *
     * @param key    操作的列表的键名
     * @param before 表示在哪个元素之前或之后插入元素
     * @param pivot  列表中的一个元素
     * @param value  是要插入的新元素
     * @return
     */
    public Long linsert(String key, boolean before, String pivot, String value) {
        send(Command.LINSERT, key, before ? "BEFORE" : "AFTER", pivot, value);
        return protocol.integer(read());
    }

    /**
     * 提供了一些用于调试和诊断的子命令，主要用于内部使用和开发调试。这些命令通常不会在生产环境中使用，而是在开发和测试阶段用于分析和调试Redis实例的问题。
     *
     * @param options 例如：OBJECT key 返回关于指定键的调试信息，包括键的类型、编码方式、Object内容的详细实现等。
     *                SEGFAULT：用于人为制造Redis服务器崩溃，通常用于测试Redis服务器的崩溃日志记录和恢复机制
     * @return
     */
    public String debug(String... options) {
        send(Command.DEBUG, options);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 原子性地从一个列表的尾部弹出一个元素，并将该元素推入到另一个列表的头部。这个操作是原子性的，即在执行期间不会被其他命令打断。
     *
     * @param source      要弹出元素的源列表
     * @param destination 要推入元素的目标列表
     * @param timeout     指定了当源列表为空时该命令的阻塞时间（单位为秒）
     * @return
     */
    public String brpoplpush(String source, String destination, int timeout) {
        send(Command.BRPOPLPUSH, source, destination, String.valueOf(timeout));
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于设置指定键的字符串值的位图中偏移量上的位的值。这个命令主要用于处理比特位集合的操作
     *
     * @param key    表示要设置位的键名
     * @param offset 表示位偏移量（从左到右的偏移位置）
     * @param value  表示要设置的位的值，只能是 0 或 1。
     * @return
     */
    public Long setbit(String key, int offset, int value) {
        send(Command.SETBIT, key, String.valueOf(offset), String.valueOf(value));
        return protocol.integer(read());
    }

    /**
     * 获取指定键的字符串值的位图中偏移量上的位的值。这个命令主要用于处理比特位集合的操作
     *
     * @param key    获取位的键名
     * @param offset 位偏移量（从左到右的偏移位置）
     * @return
     */
    public Long getbit(String key, int offset) {
        send(Command.GETBIT, key, String.valueOf(offset));
        return protocol.integer(read());
    }

    /**
     * BITPOS 命令用于查找指定键的字符串值的位图中，从左到右第一个设置为给定值的位的偏移量。这个命令可以用于处理比特位集合的操作
     *
     * @param key 操作的键名
     * @param bit 要查找的比特位值（0 或 1）
     * @return
     */
    public Long bitpos(String key, int bit) {
        send(Command.BITPOS, key, String.valueOf(bit));
        return protocol.integer(read());
    }

    /**
     * BITPOS 命令用于查找指定键的字符串值的位图中，从左到右第一个设置为给定值的位的偏移量。这个命令可以用于处理比特位集合的操作
     *
     * @param key   操作的键名
     * @param bit   要查找的比特位值（0 或 1）
     * @param start 指定搜索的范围最小
     * @param end   指定搜索的范围最大
     * @return
     */
    public Long bitpos(String key, int bit, int start, int end) {
        send(Command.BITPOS, key, String.valueOf(bit), String.valueOf(start), String.valueOf(end));
        return protocol.integer(read());
    }

    /**
     * 将指定键的字符串值从指定偏移量开始的部分替换为另一个字符串
     *
     * @param key    替换的键名
     * @param offset 要替换的起始偏移量
     * @param value  要替换的字符串值
     * @return
     */
    public Long setrange(String key, int offset, String value) {
        send(Command.SETRANGE, key, String.valueOf(offset), value);
        return protocol.integer(read());
    }

    /**
     * 用于执行 Lua 脚本。通过 EVAL 命令，可以在 Redis 服务器端执行一些复杂的逻辑，对键进行操作
     *
     * @param script  是要执行的 Lua 脚本,例如：return redis.call('INCR', KEYS[1])
     * @param numkeys 是脚本中用于访问的键数量，例如：1
     * @param key     表示脚本中用到的键名，例如：myincr
     * @param arg     表示脚本中用到的其他参数,可选
     * @return
     */
    public String eval(String script, int numkeys, String[] key, String[] arg) {
        int keyLength = key == null ? 0 : key.length;
        int argLength = arg == null ? 0 : arg.length;
        String[] array = new String[argLength + keyLength + 2];
        array[0] = script;
        array[1] = String.valueOf(numkeys);
        for (int i = 0; i < keyLength; i++) {
            array[2 + i] = key[i];
        }
        for (int i = 0; i < argLength; i++) {
            array[2 + keyLength] = arg[i];
        }
        send(Command.EVAL, array);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 用于执行存储在 Redis 服务器中的 Lua 脚本的 SHA1 校验和
     *
     * @param sha1    SHA1校验和
     * @param numkeys 表示脚本中用于访问的键数量
     * @param key     表示脚本中用到的键名
     * @param arg     表示脚本中用到的其他参数
     * @return
     */
    public String evalsha(String sha1, int numkeys, String[] key, String[] arg) {
        int keyLength = key == null ? 0 : key.length;
        int argLength = arg == null ? 0 : arg.length;
        String[] array = new String[argLength + keyLength + 2];
        array[0] = sha1;
        array[1] = String.valueOf(numkeys);
        for (int i = 0; i < keyLength; i++) {
            array[2 + i] = key[i];
        }
        for (int i = 0; i < argLength; i++) {
            array[2 + keyLength] = arg[i];
        }
        send(Command.EVALSHA, array);
        return new String(protocol.bulkString(read()));
    }


    /**
     * 在 Redis 服务器上执行 Lua 脚本
     *
     * @param script
     * @return
     */
    public String script(String... script) {
        int scriptLength = script.length;
        String[] array = new String[scriptLength + 1];
        array[0] = "LOAD";
        for (int i = 0; i < scriptLength; i++) {
            array[1 + i] = script[i];
        }
        send(Command.SCRIPT, array);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 查看 Redis 服务器的慢日志信息
     *
     * @param subcommand GET ：获取慢日志列表。
     *                   LEN ：获取慢日志的数量。
     *                   RESET ：重置慢日志的计数器。
     * @param argument   count ：要获取的慢日志条目数量。
     *                   <p>
     *                   每个慢日志条目包含以下信息：
     *                   id ：慢日志唯一标识。
     *                   timestamp ：慢日志记录的时间戳。
     *                   duration ：命令执行持续时间，以微秒为单位。
     *                   command ：执行的命令及其参数。
     * @return 对于 GET 子命令，返回慢日志列表。
     * 对于 LEN 子命令，返回慢日志的数量。
     * 对于 RESET 子命令，返回 OK。
     */
    public String slowlog(String subcommand, String argument) {
        send(Command.SLOWLOG, subcommand, argument);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 用于统计字符串值中 位（bit） 的数量
     *
     * @param key 要操作的键名
     * @return
     */
    public Long bitpos(String key) {
        return bitpos(key, 0, -1);
    }

    /**
     * 用于统计字符串值中 位（bit） 的数量
     *
     * @param key   要操作的键名
     * @param start 起始偏移量（以字节为单位），可选参数，默认为 0
     * @param end   结束偏移量（以字节为单位），可选参数，默认为 -1，表示整个字符串
     * @return
     */
    public Long bitpos(String key, int start, int end) {
        send(Command.BITCOUNT, key, String.valueOf(start), String.valueOf(end));
        return protocol.integer(read());
    }

    /**
     * 对一个或多个位图进行位运算，并将结果保存到指定的目标位图中。BITOP 支持的位运算操作包括 AND（与）、OR（或）、XOR（异或）和 NOT（非）
     *
     * @param operation 要执行的位运算操作，可以是 AND、OR、XOR 或 NOT
     * @param destkey   保存结果的目标位图键名
     * @param key       要参与位运算的位图键名，可以指定一个或多个
     * @return
     */
    public Long bitop(String operation, String destkey, String... key) {
        int keyLength = key.length;
        String[] array = new String[keyLength + 2];
        array[0] = operation;
        array[1] = destkey;
        for (int i = 0; i < keyLength; i++) {
            array[2 + i] = key[i];
        }
        send(Command.BITOP, array);
        return protocol.integer(read());
    }

    /**
     * 用于管理和监控 Redis Sentinel 守护程序的命令，这些命令可以用来查询监控信息、配置参数、执行故障转移和其他管理任务
     *
     * @param options MASTERS - 获取当前 Sentinel 监视的所有主节点的信息
     *                MASTER <master-name> - 获取指定主节点的信息，如 IP 地址、端口号、运行状态、故障转移状态等
     *                SLAVES <master-name> - 获取指定主节点的所有从节点信息
     *                GET-MASTER-ADDR-BY-NAME <master-name> - 获取指定主节点的 IP 地址和端口号
     *                FAILOVER <master-name> - 手动发起故障转移，将指定主节点切换到一个新的从节点上来提供服务
     * @return
     */
    public String sentinel(String... options) {
        send(Command.SENTINEL, options);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 序列化给定 key，并返回序列化值。序列化后的值通常用于备份和迁移数据，可以将序列化后的值存储到磁盘或者通过网络传输到另一个 Redis 服务器
     *
     * @param key 要序列化的 Redis 键
     * @return
     */
    public String dump(String key) {
        send(Command.DUMP, key);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 用于将经 DUMP 命令序列化得到的值反序列化，并将它恢复到 Redis 中特定的键
     *
     * @param key             要恢复的键名
     * @param ttl             键的生存时间（单位为毫秒），如果不需要设置过期时间，可以使用 0
     * @param serializedValue 经 DUMP 命令序列化得到的值
     * @param replace         参数是可选的，用于指示如果键已经存在，是否替换其原有值
     * @return
     */
    public String restore(String key, long ttl, String serializedValue, boolean replace) {
        String[] array = new String[3 + (replace ? 1 : 0)];
        array[0] = key;
        array[1] = String.valueOf(ttl);
        array[2] = serializedValue;
        if (replace) {
            array[3] = "REPLACE";
        }
        send(Command.RESTORE, array);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 用于设置键的过期时间，单位为毫秒。当设置了过期时间后，Redis 会在指定的时间段后自动删除该键。
     *
     * @param key          设置过期时间的键名
     * @param milliseconds 键的过期时间，以毫秒为单位
     * @return
     */
    public Long pexpire(String key, long milliseconds) {
        send(Command.PEXPIRE, key, String.valueOf(milliseconds));
        return protocol.integer(read());
    }

    /**
     * 用于设置键的过期时间戳，单位为毫秒。与 PEXPIRE 命令不同，PEXPIREAT 命令可以设置一个绝对的过期时间点
     *
     * @param key                   要设置过期时间的键名
     * @param millisecondsTimestamp 是过期时间点的时间戳，单位为毫秒
     * @return
     */
    public Long pexpireat(String key, long millisecondsTimestamp) {
        send(Command.PEXPIREAT, key, String.valueOf(millisecondsTimestamp));
        return protocol.integer(read());
    }

    /***
     * 用于获取键的剩余过期时间，以毫秒为单位。该命令返回的是指定键的剩余过期时间，如果键不存在或者没有设置过期时间，PTTL 命令会返回 -1
     * @param key 要获取剩余过期时间的键名
     * @return
     */
    public Long pttl(String key) {
        send(Command.PTTL, key);
        return protocol.integer(read());
    }

    /**
     * 将键存储的值以浮点数进行增加。如果键不存在，那么在执行 INCRBYFLOAT 命令前，键的值会被设置为 0。 INCRBYFLOAT 命令的语法如下所示
     *
     * @param key       要进行增加操作的键名
     * @param increment 要增加的浮点数值
     * @return
     */
    public Long incrbyfloat(String key, float increment) {
        send(Command.INCRBYFLOAT, key, String.valueOf(increment));
        return protocol.integer(read());
    }

    /**
     * 用于设置指定键的值，并在指定的毫秒数后自动删除该键。该命令与 SETEX 命令类似，但它接受的过期时间参数的单位是毫秒而不是秒
     *
     * @param key          要设置值的键名
     * @param milliseconds 过期时间，以毫秒为单位
     * @param value        要设置的值
     * @return
     */
    public Long psetex(String key, long milliseconds, String value) {
        send(Command.PSETEX, key, String.valueOf(milliseconds), value);
        return protocol.integer(read());
    }

    /**
     * 用于查看和管理客户端连接到 Redis 服务器的情况。它包括一系列子命令，用于获取当前连接的客户端信息以及对客户端进行一些管理操作
     *
     * @param options LIST：获取连接到服务器的客户端连接列表及相关信息
     *                GETNAME：获取客户端连接的名称
     *                SETNAME：设置客户端连接的名称
     *                KILL：关闭指定 IP 和端口的客户端连接
     *                PAUSE：使服务端在指定时间内停止处理来自客户端的请求
     * @return
     */
    public String client(String... options) {
        send(Command.CLIENT, options);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 获取服务器当前的时间，返回一个包含两个元素的数组，分别是当前时间的秒数和微秒数
     *
     * @return
     */
    public List<String> time() {
        send(Command.TIME, new String[]{});
        return protocol.strings(read());
    }

    /**
     * 将指定的键从一个 Redis 实例迁移到另一个 Redis 实例。这个命令对于 Redis 集群和分片操作非常有用，可以用于动态地迁移键到不同的实例上
     *
     * @param host          目标 Redis 实例的主机名或 IP 地址
     * @param port          目标 Redis 实例的端口号
     * @param key           要迁移的键名
     * @param destinationDb 目标 Redis 实例中的数据库索引
     * @param timeout       超时时间，以毫秒为单位，用于设置迁移操作的超时时间
     * @param copy          可选参数，表示是否保留原始实例中的键
     * @param replace       可选参数，表示如果目标实例中已存在同名键是否要替换
     * @return
     */
    public String migrate(String host, int port, String key, int destinationDb, String timeout, boolean copy, boolean replace) {
        String[] array = new String[5 + (copy ? 1 : 0) + (replace ? 1 : 0)];
        array[0] = host;
        array[1] = String.valueOf(port);
        array[2] = key;
        array[3] = String.valueOf(destinationDb);
        array[4] = String.valueOf(timeout);
        if (copy) {
            array[5] = "COPY";
        }
        if (replace) {
            array[6] = "REPLACE";
        }
        send(Command.MIGRATE, array);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 在哈希数据结构中对存储浮点数的字段进行增减操作的命令
     *
     * @param key       哈希表的名称
     * @param field     哈希表中的字段名
     * @param increment 浮点数的增量
     * @return
     */
    public String hincrbyfloat(String key, String field, float increment) {
        send(Command.HINCRBYFLOAT, key, field, String.valueOf(increment));
        return new String(protocol.bulkString(read()));
    }

    /**
     * 用于遍历数据库中的键的命令，它支持在不阻塞服务器的情况下逐步遍历数据库中的键空间
     *
     * @param cursor  是一个用于迭代的游标，初始值通常为 0
     * @param pattern 一个可选参数，用于指定匹配的键名模式。只有匹配的键名才会返回
     * @param count   一个可选参数，用于指定每次迭代返回的键数量
     * @return
     */
    public List<String> scan(int cursor, String pattern, int count) {
        send(Command.SCAN, String.valueOf(cursor), pattern, String.valueOf(count));
        return protocol.strings(read());
    }

    /**
     * 用于遍历数据库中的键的命令，它支持在不阻塞服务器的情况下逐步遍历数据库中的键空间
     *
     * @param cursor 是一个用于迭代的游标，初始值通常为 0
     * @return
     */
    public List<String> scan(int cursor) {
        send(Command.SCAN, String.valueOf(cursor));
        return protocol.strings(read());
    }

    /**
     * 用于遍历哈希表的命令，它用于迭代访问哈希表中的键值对
     *
     * @param key     哈希表的名称
     * @param cursor  是一个用于迭代的游标，初始值通常为 0
     * @param pattern 一个可选参数，用于指定匹配的键名模式。只有匹配的键名才会返回
     * @param count   一个可选参数，用于指定每次迭代返回的键数量
     * @return
     */
    public List<String> hscan(String key, int cursor, String pattern, int count) {
        send(Command.HSCAN, key, String.valueOf(cursor), pattern, String.valueOf(count));
        return protocol.strings(read());
    }

    /**
     * 用于遍历哈希表的命令，它用于迭代访问哈希表中的键值对
     *
     * @param key    哈希表的名称
     * @param cursor 是一个用于迭代的游标，初始值通常为 0
     * @return
     */
    public List<String> hscan(String key, int cursor) {
        send(Command.HSCAN, key, String.valueOf(cursor));
        return protocol.strings(read());
    }

    /**
     * 遍历集合（Set）中的元素的命令，它支持在不阻塞服务器的情况下逐步遍历集合中的元素
     *
     * @param key     哈希表的名称
     * @param cursor  是一个用于迭代的游标，初始值通常为 0
     * @param pattern 一个可选参数，用于指定匹配的键名模式。只有匹配的键名才会返回
     * @param count   一个可选参数，用于指定每次迭代返回的键数量
     * @return
     */
    public List<String> sscan(String key, int cursor, String pattern, int count) {
        send(Command.SSCAN, key, String.valueOf(cursor), pattern, String.valueOf(count));
        return protocol.strings(read());
    }

    /**
     * 遍历集合（Set）中的元素的命令，它支持在不阻塞服务器的情况下逐步遍历集合中的元素
     *
     * @param key    哈希表的名称
     * @param cursor 是一个用于迭代的游标，初始值通常为 0
     * @return
     */
    public List<String> sscan(String key, int cursor) {
        send(Command.SSCAN, key, String.valueOf(cursor));
        return protocol.strings(read());
    }

    /**
     * 遍历有序集合（Sorted Set）中的成员的命令，它支持在不阻塞服务器的情况下逐步遍历有序集合中的成员
     *
     * @param key     哈希表的名称
     * @param cursor  是一个用于迭代的游标，初始值通常为 0
     * @param pattern 一个可选参数，用于指定匹配的键名模式。只有匹配的键名才会返回
     * @param count   一个可选参数，用于指定每次迭代返回的键数量
     * @return
     */
    public List<String> zscan(String key, int cursor, String pattern, int count) {
        send(Command.ZSCAN, key, String.valueOf(cursor), pattern, String.valueOf(count));
        return protocol.strings(read());
    }

    /**
     * 遍历有序集合（Sorted Set）中的成员的命令，它支持在不阻塞服务器的情况下逐步遍历有序集合中的成员
     *
     * @param key    哈希表的名称
     * @param cursor 是一个用于迭代的游标，初始值通常为 0
     * @return
     */
    public List<String> zscan(String key, int cursor) {
        send(Command.ZSCAN, key, String.valueOf(cursor));
        return protocol.strings(read());
    }

    /**
     * 等待（阻塞）直到指定数量的从节点（replica）进行复制操作并确认复制的数据量达到指定的要求
     *
     * @param numreplicas 是一个整数，表示要等待的从节点数量
     * @param timeout     是一个整数，表示等待的超时时间（以毫秒为单位）
     * @return
     */
    public Long wait(int numreplicas, long timeout) {
        send(Command.WAIT, String.valueOf(numreplicas), String.valueOf(timeout));
        return protocol.integer(read());
    }


    /**
     * 用于管理和操作集群
     *
     * @param option INFO：获取关于Redis Cluster的信息，如集群节点数量、槽位分布情况等
     *               NODES：获取当前集群中所有节点的信息，包括节点ID、IP地址、端口号、角色等
     *               MEET：将一个节点添加到集群中，并指定它要连接的主节点的IP地址和端口号
     *               ADDSLOTS：将一个或多个槽位分配给当前节点
     *               DELSLOTS：从当前节点中删除指定的槽位
     *               REPLICATE：将当前节点设置为指定节点的从节点
     *               FORGET：从集群中移除指定节点
     *               KEYSLOT：计算给定键的哈希槽位
     *               COUNTKEYSINSLOT：计算指定槽位中的键数量
     * @return
     */
    public String cluster(String option) {
        send(Command.CLUSTER, option);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 在事务模式下执行指定命令的命令
     *
     * @return
     */
    public String asking() {
        send(Command.ASKING, new String[]{});
        return new String(protocol.bulkString(read()));
    }

    /**
     * 一种特殊数据类型 HyperLogLog 的命令，用于将一个或多个元素添加到 HyperLogLog 数据结构中
     *
     * @param key     HyperLogLog 数据结构的键名
     * @param element 要添加到 HyperLogLog 中的元素
     * @return
     */
    public Long pfadd(String key, String... element) {
        int elementLength = element == null ? 0 : element.length;
        String[] array = new String[1 + elementLength];
        array[0] = key;
        for (int i = 0; i < elementLength; i++) {
            array[1 + i] = element[i];
        }
        send(Command.PFADD, array);
        return protocol.integer(read());
    }

    /**
     * 获取 HyperLogLog 数据结构中近似不重复元素数量的命令
     *
     * @param key HyperLogLog 数据结构的键名
     * @return
     */
    public Long pfcount(String... key) {
        send(Command.PFCOUNT, key);
        return protocol.integer(read());
    }

    /**
     * 合并多个 HyperLogLog 数据结构，生成一个新的 HyperLogLog 数据结构
     *
     * @param destkey   合并后生成的新 HyperLogLog 数据结构的键名
     * @param sourceKey 要合并的 HyperLogLog 数据结构的键名
     * @return
     */
    public String pfmerge(String destkey, String... sourceKey) {
        int sourceKeyLength = sourceKey == null ? 0 : sourceKey.length;
        String[] array = new String[1 + sourceKeyLength];
        array[0] = destkey;
        for (int i = 0; i < sourceKeyLength; i++) {
            array[1 + i] = sourceKey[i];
        }
        send(Command.PFMERGE, array);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于将 Redis 实例设置为只读模式
     *
     * @return
     */
    public String readonly() {
        send(Command.READONLY, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 用于取消将 Redis 实例设置为只读模式，使其重新变为读写模式
     *
     * @return
     */
    public String readwrite() {
        send(Command.READWRITE, new String[]{});
        return new String(protocol.simpleString(read()));
    }

    /**
     * 将经度和纬度坐标存储在一个给定的地理空间索引中。它用于处理地理位置信息和相关的地理坐标数据
     *
     * @param key       存储地理位置数据的键名
     * @param longitude 经度值
     * @param latitude  纬度值
     * @param member    是与给定坐标相关联的成员名称
     * @return
     */
    public Long geoadd(String key, double[] longitude, double[] latitude, String[] member) {
        int longitudeLength = longitude == null ? 0 : longitude.length;
        int latitudeLength = latitude == null ? 0 : latitude.length;
        int memberLength = member == null ? 0 : member.length;
        String[] array = new String[1 + longitudeLength + latitudeLength + memberLength];
        array[0] = key;
        for (int i = 0; i < longitudeLength; i++) {
            array[1 + i] = String.valueOf(longitude[i]);
        }
        for (int i = 0; i < latitudeLength; i++) {
            array[1 + longitudeLength + i] = String.valueOf(latitude[i]);
        }
        for (int i = 0; i < memberLength; i++) {
            array[1 + longitudeLength + latitudeLength + i] = String.valueOf(member[i]);
        }
        send(Command.GEOADD, array);
        return protocol.integer(read());
    }


    /**
     * 计算地理位置的距离
     *
     * @param key     地理位置的键名
     * @param member1 地理位置索引中成员名称
     * @param member2 地理位置索引中成员名称
     * @return
     */
    public String geodist(String key, String member1, String member2) {
        send(Command.GEODIST, key, member1, member2);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 获取地理位置的 GeoHash 值
     *
     * @param key    地理位置的键名
     * @param member 地理位置索引中的成员名称
     * @return
     */
    public List<String> geohash(String key, String... member) {
        int memberLength = member == null ? 0 : member.length;
        String[] array = new String[1 + memberLength];
        array[0] = key;
        for (int i = 0; i < memberLength; i++) {
            array[1 + i] = member[i];
        }
        send(Command.GEOHASH, array);
        return protocol.strings(read());
    }

    /**
     * 获取地理位置的经度和纬度信息
     *
     * @param key    地理位置的键名
     * @param member 地理位置索引中的成员名称
     * @return
     */
    public List<String> geopos(String key, String... member) {
        int memberLength = member == null ? 0 : member.length;
        String[] array = new String[1 + memberLength];
        array[0] = key;
        for (int i = 0; i < memberLength; i++) {
            array[1 + i] = member[i];
        }
        send(Command.GEOPOS, array);
        return protocol.strings(read());
    }

    /**
     * 根据给定的中心位置和半径范围，查询符合条件的地理位置成员
     *
     * @param key       地理位置的键名
     * @param longitude 中心位置的经度
     * @param latitude  中心位置的纬度
     * @param radius    半径范围
     * @param unit      单位（支持m、km、miles和feet）
     * @param options   [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]
     * @return
     */
    public List<String> georadius(String key, double longitude, double latitude, int radius, String unit, String[] options) {
        int optionsLength = options == null ? 0 : options.length;
        String[] array = new String[5 + optionsLength];
        array[0] = key;
        array[1] = String.valueOf(longitude);
        array[2] = String.valueOf(latitude);
        array[3] = String.valueOf(radius);
        array[4] = String.valueOf(unit);
        for (int i = 0; i < optionsLength; i++) {
            array[5 + i] = options[i];
        }
        send(Command.GEORADIUS, array);
        return protocol.strings(read());
    }

    /**
     * 只读版本的 GEORADIUS 命令，用于在不进行写操作的情况下执行地理位置的范围查询
     *
     * @param key       地理位置的键名
     * @param longitude 中心位置的经度
     * @param latitude  中心位置的纬度
     * @param radius    半径范围
     * @param unit      单位（支持m、km、miles和feet）
     * @param options   [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]
     * @return
     */
    public List<String> georadius_ro(String key, double longitude, double latitude, int radius, String unit, String[] options) {
        int optionsLength = options == null ? 0 : options.length;
        String[] array = new String[5 + optionsLength];
        array[0] = key;
        array[1] = String.valueOf(longitude);
        array[2] = String.valueOf(latitude);
        array[3] = String.valueOf(radius);
        array[4] = String.valueOf(unit);
        for (int i = 0; i < optionsLength; i++) {
            array[5 + i] = options[i];
        }
        send(Command.GEORADIUS_RO, array);
        return protocol.strings(read());
    }

    /**
     * 通过给定的地理位置成员名，查询符合指定半径范围内的其他地理位置成员
     *
     * @param key     地理位置的键名
     * @param member  地理位置成员名
     * @param radius  半径范围
     * @param unit    单位（支持m、km、miles和feet）
     * @param options [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]
     * @return
     */
    public List<String> georadiusbymember(String key, String member, int radius, String unit, String[] options) {
        int optionsLength = options == null ? 0 : options.length;
        String[] array = new String[4 + optionsLength];
        array[0] = key;
        array[1] = member;
        array[2] = String.valueOf(radius);
        array[3] = String.valueOf(unit);
        for (int i = 0; i < optionsLength; i++) {
            array[4 + i] = options[i];
        }
        send(Command.GEORADIUSBYMEMBER, array);
        return protocol.strings(read());
    }

    /**
     * 一个只读版本的 GEORADIUSBYMEMBER 命令，用于在不进行写操作的情况下执行以地理位置成员为中心的范围查询
     *
     * @param key     地理位置的键名
     * @param member  地理位置成员名
     * @param radius  半径范围
     * @param unit    单位（支持m、km、miles和feet）
     * @param options [WITHCOORD] [WITHDIST] [WITHHASH] [COUNT count] [ASC|DESC] [STORE key] [STOREDIST key]
     * @return
     */
    public List<String> georadiusbymember_ro(String key, String member, int radius, String unit, String[] options) {
        int optionsLength = options == null ? 0 : options.length;
        String[] array = new String[4 + optionsLength];
        array[0] = key;
        array[1] = member;
        array[2] = String.valueOf(radius);
        array[3] = String.valueOf(unit);
        for (int i = 0; i < optionsLength; i++) {
            array[4 + i] = options[i];
        }
        send(Command.GEORADIUSBYMEMBER_RO, array);
        return protocol.strings(read());
    }

    /**
     * 管理和加载 Redis 模块的命令。Redis 模块允许扩展和定制 Redis 的功能，它可以通过动态加载的方式添加新的命令、数据结构、功能和行为
     *
     * @param options LOAD <path>：加载指定路径下的 Redis 模块。<path> 是模块的文件路径或名称
     *                UNLOAD <name>：卸载指定名称的 Redis 模块
     *                LIST：列出已加载的 Redis 模块
     *                INFO <name>：获取指定名称的 Redis 模块的信息
     *                <p>
     *                注意：模块的可用性取决于使用的 Redis 版本和是否启用了动态加载模块的功能（通过 --loadmodule <path> 选项启动 Redis）
     * @return
     */
    public String module(String... options) {
        send(Command.MODULE, options);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 对字符串（String）类型的位（bit）操作的命令。它允许你直接在位级别上进行操作，进行位的设置、获取和修改等操作
     *
     * @param key         进行位操作的字符串键名
     * @param subcommands GET type offset：获取指定偏移位置的位的值,例如：GET u8 0
     *                    SET type offset value：设置指定偏移位置的位的值,例如：SET u8 0 42
     *                    INCRBY type offset increment：对指定偏移位置的位的值进行增量增加或减少,例如：INCRBY i32 0 10
     *                    OVERFLOW WRAP|SAT|FAIL：用于指定位操作的溢出行为，可以选择包装（WRAP）、饱和（SAT）或失败（FAIL）,例如：OVERFLOW SAT
     * @return
     */
    public List<String> bitfield(String key, String... subcommands) {
        int optionsLength = subcommands == null ? 0 : subcommands.length;
        String[] array = new String[1 + optionsLength];
        array[0] = key;
        for (int i = 0; i < optionsLength; i++) {
            array[1 + i] = subcommands[i];
        }
        send(Command.BITFIELD, array);
        return protocol.strings(read());
    }

    /**
     * 获取哈希表（Hash）中指定字段的值的长度
     *
     * @param key   哈希表的键名
     * @param field 获取长度的字段名
     * @return
     */
    public Long hstrlen(String key, String field) {
        send(Command.HSTRLEN, key, field);
        return protocol.integer(read());
    }

    /**
     * 交换两个数据库的数据,默认情况下有 16 个数据库（编号为 0 到 15）
     *
     * @param index1
     * @param index2
     * @return
     */
    public String swapdb(int index1, int index2) {
        send(Command.SWAPDB, String.valueOf(index1), String.valueOf(index2));
        return new String(protocol.simpleString(read()));
    }

    /**
     * 管理内存相关操作的命令集合
     *
     * @param options USAGE key：查看指定键所占用的内存大小
     * @return
     */
    public String memory(String... options) {
        send(Command.MEMORY, options);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 向流数据结构中添加条目
     *
     * @param key   流的名称
     * @param id    指定条目的 ID。如果使用 * 作为 ID，则表示由 Redis 自动生成唯一的 ID
     * @param value 条目的字段和相应的值
     * @return
     */
    public String xadd(String key, String id, Map<String, String> value) {
        String[] array = new String[2 + value.size() * 2];
        array[0] = key;
        array[1] = id;
        int index = 1;
        for (String field : value.keySet()) {
            index++;
            array[index] = field;
            index++;
            array[index] = value.get(field);
        }
        send(Command.XADD, array);
        return new String(protocol.bulkString(read()));
    }

    /**
     * 获取指定流中的条目数量
     *
     * @param streamName 目标流的名称
     * @return
     */
    public Long xlen(String streamName) {
        send(Command.XLEN, streamName);
        return protocol.integer(read());
    }

    /**
     * 对流数据结构进行修剪（删除）操作
     *
     * @param streamName 目标流的名称
     * @param condition  ~ 表示保留小于等于 minid 的条目，+ 表示保留大于 minid 的条目
     * @param minid      表示要保留的最小ID
     * @return
     */
    public Long xtrim(String streamName, String condition, int minid) {
        send(Command.XTRIM, streamName, "MAXLEN", condition, String.valueOf(minid));
        return protocol.integer(read());
    }

    /**
     * 获取范围内的流数据条目
     *
     * @param key
     * @param start
     * @param end
     * @param count
     * @return
     */
    public Map<String, String> xrange(String key, String start, String end, int count) {
        send(Command.XRANGE, key, start, end, "", "COUNT", String.valueOf(count));
        return protocol.hashMap(read());
    }


    /**
     * 按照逆序获取指定范围的消息流中的元素
     *
     * @param streamName 消息流的名称
     * @param endId      消息范围开始
     * @param startId    消息范围结束
     * @param count      指定要获取的最大元素数量，如不需此参数值为：-1
     * @return 如果有错误，返回的结果是“-WRONGTYPE”开头的
     */
    public List<String> xrevrange(String streamName, String endId, String startId, int count) {
        String[] array = new String[3 + (count == -1 ? 0 : 2)];
        array[0] = streamName;
        array[1] = endId;
        array[2] = startId;
        if (count != -1) {
            array[3] = "COUNT";
            array[4] = String.valueOf(count);
        }
        send(Command.XREVRANGE, array);
        return protocol.strings(read(1024 * 1024));
    }

    /**
     * 用于读取和消费 Redis Streams 数据结构中的消息,它可以按照消息流的ID来读取特定范围内的消息，并以数组的形式返回这些消息
     *
     * @param count        指定要读取的最大消息元素数量
     * @param milliseconds 用于指定如果没有可读取的消息时，命令应该阻塞的时间
     * @param key          消息流的名称
     * @param id           指定要读取的消息的ID
     * @return
     */
    public List<String> xread(int count, long milliseconds, String[] key, String[] id) {
        int keSize = key == null ? 0 : key.length;
        int idSize = id == null ? 0 : id.length;
        String[] array = new String[5 + keSize + idSize];
        array[0] = "COUNT";
        array[1] = String.valueOf(count);
        array[2] = "BLOCK";
        array[3] = String.valueOf(milliseconds);
        array[4] = "STREAMS";
        for (int i = 0; i < keSize; i++) {
            array[5 + i] = key[i];
        }
        for (int i = 0; i < idSize; i++) {
            array[5 + keSize + i] = id[i];
        }
        send(Command.XREAD, array);
        return protocol.strings(read(1024 * 1024));
    }

    /**
     * 用于读取和消费 Redis Streams 数据结构中的消息,它可以按照消息流的ID来读取特定范围内的消息，并以数组的形式返回这些消息
     *
     * @param key 消息流的名称
     * @param id  指定要读取的消息的ID
     * @return
     */
    public List<String> xread(String[] key, String[] id) {
        int keSize = key == null ? 0 : key.length;
        int idSize = id == null ? 0 : id.length;
        String[] array = new String[1 + keSize + idSize];
        array[0] = "STREAMS";
        for (int i = 0; i < keSize; i++) {
            array[1 + i] = key[i];
        }
        for (int i = 0; i < idSize; i++) {
            array[1 + keSize + i] = id[i];
        }
        send(Command.XREAD, array);
        return protocol.strings(read(1024 * 1024));
    }

    /**
     * 向 Redis Streams 数据结构发送确认消息已经成功处理的信号
     * 它标记一个或多个消息已经被消费者处理完成，以便 streams 中的消息可以被清理或进一步处理。
     *
     * @param key   消息流的名称
     * @param group 标识消费者组
     * @param id    是一个或多个消息的 ID，用于指定要确认处理完成的消息,可选参数
     * @return
     */
    public Long xack(String key, String group, String... id) {
        int idSize = id == null ? 0 : id.length;
        String[] array = new String[2 + idSize];
        array[0] = key;
        array[1] = group;
        for (int i = 0; i < idSize; i++) {
            array[2 + i] = id[i];
        }
        send(Command.XACK, array);
        return protocol.integer(read());
    }

    /**
     * 管理消费者组的命令。它用于创建、设置和删除消费者组
     *
     * @param subcommand 1.创建一个消费者组：CREATE stream group id-or-$ [MKSTREAM]<br/>
     *                   stream：要创建消费者组的消息流的名称。<br/>
     *                   group：要创建的消费者组的名称。<br/>
     *                   id-or-$：指定消费者组的起始 ID，可以是指定的消息 ID，也可以使用 $ 表示最新的消息ID。<br/>
     *                   MKSTREAM：可选参数，如果指定了该选项并且消息流不存在，将会自动创建该消息流。<p>
     *                   <p>
     *                   <p>
     *                   2.销毁一个消费者组：XGROUP DESTROY stream group<br/>
     *                   stream：要销毁消费者组的消息流的名称。<br/>
     *                   group：要销毁的消费者组的名称。<p>
     *                   <p>
     *                   <p>
     *                   3.设置消费者组中某个消费者的待处理消息最新 ID：XGROUP SETID stream group id-or-$<br/>
     *                   stream：要创建消费者组的消息流的名称。<br/>
     *                   group：要创建的消费者组的名称。<br/>
     *                   id-or-$：指定消费者组的起始 ID，可以是指定的消息 ID，也可以使用 $ 表示最新的消息ID。<p>
     *                   <p>
     *                   <p>
     *                   4.设置消费者组中某个消费者的消费者挂起状态：XGROUP SETID stream group consumer [NOACK|JUSTID]<br/>
     *                   stream：消息流的名称。<br/>
     *                   group：消费者组的名称。<br/>
     *                   consumer：消费者的名称。<br/>
     *                   NOACK：可选参数，设置消费者为挂起状态。<br/>
     *                   JUSTID：可选参数，只返回消费者的 ID，而不设置状态。<br/>
     * @return
     */
    public String xgroup(String... subcommand) {
        send(Command.XGROUP, subcommand);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 创建一个消费者组
     *
     * @param stream 消息流的名称
     * @param group  消费者组的名称
     * @param id     消费者组的起始 ID，可以是指定的消息 ID，也可以使用 $ 表示最新的消息ID
     * @return
     */
    public String xgroupcreate(String stream, String group, String id) {
        send(Command.XGROUP, "CREATE", stream, group, id);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 销毁一个消费者组
     *
     * @param stream 消息流的名称
     * @param group  消费者组的名称
     * @return
     */
    public Long xgroupdestroy(String stream, String group) {
        send(Command.XGROUP, "DESTROY", stream, group);
        return protocol.integer(read());
    }

    /**
     * 设置消费者组中某个消费者的待处理消息最新 ID
     *
     * @param stream 消息流的名称
     * @param group  消费者组的名称
     * @param id     消费者组的起始 ID，可以是指定的消息 ID，也可以使用 $ 表示最新的消息ID
     * @return
     */
    public String xgroupsetid(String stream, String group, String id) {
        send(Command.XGROUP, "SETID", stream, group, id);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 设置消费者组中某个消费者的消费者挂起状态
     *
     * @param stream   消息流的名称
     * @param group    消费者组的名称
     * @param consumer 消费者的名称
     * @param noack    可选参数，设置消费者为挂起状态
     * @param justid   可选参数，只返回消费者的 ID，而不设置状态
     * @return
     */
    public String xgroupsetid(String stream, String group, String consumer, boolean noack, boolean justid) {
        send(Command.XGROUP, "SETID", stream, group, consumer, noack ? "NOACK" : null, justid ? "JUSTID" : null);
        return new String(protocol.simpleString(read()));
    }

    /**
     * 从 Redis Streams 数据结构中以消费者群组的形式读取消息的命令
     *
     * @param group        示消费者群组的名称
     * @param consumer     消费者的名称
     * @param count        可选参数，表示返回的消息数量
     * @param milliseconds 可选参数，表示在没有可用消息时，阻塞的时间
     * @param key          表示消息流的键名
     * @param id           读取消息的起始 ID，可以指定多个消息流的 ID
     * @return
     */
    public List<String> xreadgroup(String group, String consumer, int count, long milliseconds, String[] key, String[] id) {
        int keSize = key == null ? 0 : key.length;
        int idSize = id == null ? 0 : id.length;
        String[] array = new String[8 + keSize + idSize];
        array[0] = "GROUP";
        array[1] = group;
        array[2] = consumer;
        array[3] = "COUNT";
        array[4] = String.valueOf(count);
        array[5] = "BLOCK";
        array[6] = String.valueOf(milliseconds);
        array[7] = "STREAMS";
        for (int i = 0; i < keSize; i++) {
            array[8 + i] = key[i];
        }
        for (int i = 0; i < idSize; i++) {
            array[8 + keSize + i] = id[i];
        }
        send(Command.XREADGROUP, array);
        return protocol.strings(read(1024 * 1024));
    }

    /**
     * 从 Redis Streams 数据结构中以消费者群组的形式读取消息的命令
     *
     * @param group    示消费者群组的名称
     * @param consumer 消费者的名称
     * @param key      表示消息流的键名
     * @param id       读取消息的起始 ID，可以指定多个消息流的 ID
     * @return
     */
    public List<String> xreadgroup(String group, String consumer, String[] key, String[] id) {
        int keSize = key == null ? 0 : key.length;
        int idSize = id == null ? 0 : id.length;
        String[] array = new String[4 + keSize + idSize];
        array[0] = "GROUP";
        array[1] = group;
        array[2] = consumer;
        array[3] = "STREAMS";
        for (int i = 0; i < keSize; i++) {
            array[4 + i] = key[i];
        }
        for (int i = 0; i < idSize; i++) {
            array[4 + keSize + i] = id[i];
        }
        send(Command.XREADGROUP, array);
        return protocol.strings(read());
    }

    /**
     * 用于获取消费者组中待处理消息的信息。它可以返回消息的数量、最老消息的时间戳、最新消息的时间戳、递增编号（序列号）、所有未确认消息及其消费者的相关信息等
     *
     * @param stream 消息流的名称
     * @param group  消费者组的名称
     * @return
     */
    public List<String> xpending(String stream, String group) {
        send(Command.XPENDING, stream, group);
        return protocol.strings(read());
    }

    /**
     * 用于获取消费者组中待处理消息的信息。它可以返回消息的数量、最老消息的时间戳、最新消息的时间戳、递增编号（序列号）、所有未确认消息及其消费者的相关信息等
     *
     * @param stream   消息流的名称
     * @param group    消费者组的名称
     * @param start    可选参数，指定开始时间戳，如果未指定，将从最旧的消息开始搜索
     * @param end      可选参数，指定结束时间戳，如果未指定将从最新的消息开始搜索
     * @param count    可选参数，指定返回的最大消息数，如果未指定将返回所有未处理的消息
     * @param consumer 可选参数，指定某个消费者的名称，如果未指定，将返回所有消息的统计信息
     * @return
     */
    public List<String> xpending(String stream, String group, String start, String end, int count, String consumer) {
        send(Command.XPENDING, stream, group, start, end, String.valueOf(count), consumer);
        return protocol.strings(read());
    }

    /**
     * 用于将消息从一个消费者组的待处理状态转移到另一个消费者组或消费者
     *
     * @param stream     消息流的名称
     * @param group      消费者组的名称
     * @param consumer   消费者的名称
     * @param time       一个表示未执行任何操作的消息所需的最短时间（以毫秒为单位）。只有在消息处于待处理状态并且超过该时间后，才能被转移到另一个消费者组或消费者
     * @param id         要转移的消息的 ID
     * @param idle       可选参数，设置消息的空闲时间（以毫秒为单位）
     * @param retrycount 可选参数，设置消息的时间戳（以毫秒为单位）
     * @param force      可选参数，设置消息的重试计数（失败尝试的次数）
     * @param justid     可选参数，即使消息不处于待处理状态，也强制执行转移操作
     * @return
     */
    public List<String> xclaim(String stream, String group, String consumer, String time, String id, String idle, String retrycount, boolean force, boolean justid) {
        send(Command.XCLAIM, stream, group, consumer, time, id, idle, retrycount, force ? "FORCE" : null, justid ? "JUSTID" : null);
        return protocol.strings(read(1024 * 1024));
    }

    /**
     * Redis 从版本 6.0 开始引入的功能，用于管理和控制对 Redis 数据库的访问权限。ACL 允许你以细粒度的方式配置用户和用户组的权限，从而控制谁可以执行哪些操作
     *
     * @param subcommands GETUSER 命令返回一个描述用户权限和规则的字符串<br/>
     *                    USERS 命令返回一个描述当前所有用户的字符串<br/>
     *                    CAT 命令返回一个描述 ACL 功能的字符串<br/>
     *                    LOG 命令返回一个描述日志的字符串<br/>
     *                    WHOAMI 命令返回当前连接客户端的用户名<br/>
     * @return
     */
    public String acl(String... subcommands) {
        send(Command.ACL, subcommands);
        return new String(protocol.bulkString(read(1024 * 1024)));
    }

    /**
     * XINFO 命令用于获取 Redis Stream（流）的信息。Stream 是 Redis 5.0 引入的一种数据结构，可用于按时间顺序保存和处理消息
     *
     * @param subcommands STREAM key：获取指定 Stream 的信息，返回一个包含有关 Stream 的各种属性的字典<br/>
     *                    GROUPS key：获取指定 Stream 的消费者组的信息，返回一个包含消费者组的各种属性的列表<br/>
     *                    CONSUMERS key groupname：获取指定 Stream 中指定消费者组的消费者信息，返回一个包含消费者的各种属性的列表<br/>
     * @return
     */
    public List<String> xinfo(String... subcommands) {
        send(Command.XINFO, subcommands);
        return protocol.strings(read());
    }

    /**
     * Redis 6.0 或更高版本,在列表（List）中查找指定元素的位置。LPOS 命令会返回该元素在列表中首次出现的索引位置
     *
     * @param key     列表的键名
     * @param element 查找的元素
     * @param rank    （可选）：指定查找第 rank 个匹配元素的位置，rank 为正数表示正序（第一个匹配元素的位置为 0），为负数表示逆序（最后一个匹配元素的位置为 -1）
     * @return
     */
    public Long lpos(String key, String element, int rank) {
        String[] array = new String[2 + (rank == 0 ? 0 : 2)];
        array[0] = key;
        array[1] = element;
        if (rank != 0) {
            array[2] = "RANK";
            array[3] = String.valueOf(rank);
        }
        send(Command.LPOS, array);
        return protocol.integer(read());
    }

    /**
     * Redis 的 BZPOPMIN 命令用于在阻塞列表中弹出具有最低分值的元素。BZPOPMIN 是 Redis 5.0 版本引入的一个阻塞命令，它是 BZPOP 的变体
     *
     * @param key     一个或多个阻塞列表的键
     * @param timeout 是超时时间（以秒为单位)
     * @return
     */
    public List<String> bzpopmin(String[] key, long timeout) {
        int keySize = key == null ? 0 : key.length;
        String[] array = new String[1 + keySize];
        for (int i = 0; i < keySize; i++) {
            array[i] = key[i];
        }
        array[keySize] = String.valueOf(timeout);
        send(Command.BZPOPMIN, array);
        return protocol.strings(read());
    }

    /**
     * Redis 的 BZPOPMAX 命令用于在阻塞列表中弹出具有最高分值的元素。BZPOPMAX 是 Redis 5.0 版本引入的一个阻塞命令，它是 BZPOP 的变体
     *
     * @param key     一个或多个阻塞列表的键
     * @param timeout 是超时时间（以秒为单位)
     * @return
     */
    public List<String> bzpopmax(String[] key, long timeout) {
        int keySize = key == null ? 0 : key.length;
        String[] array = new String[1 + keySize];
        for (int i = 0; i < keySize; i++) {
            array[i] = key[i];
        }
        array[keySize] = String.valueOf(timeout);
        send(Command.BZPOPMAX, array);
        return protocol.strings(read());
    }


    /**
     * 获取 Redis 服务器的角色
     * 服务器角色的字符串表示，可以是 “master”、“slave” 或者 “single”。
     * 如果服务器是主服务器或从服务器，则返回该服务器所在线上的客户端个数。
     * 如果服务器是从服务器，则返回与其连接的主服务器的信息。
     * 请注意，该命令仅在 Redis 2.8 及以上版本中可用。
     *
     * @return
     */
    public List<String> role() {
        send(Command.ROLE, new String[]{});
        return protocol.strings(read());
    }

    /**
     * 是否为空
     *
     * @param value
     * @return
     */
    public boolean isEmpty(String value) {
        return value == null || value.length() == 0;
    }

    @Override
    public void close() {
        if (channel != null) {
            channel.close();
            channel = null;
        }
        unsubscribe();
        if (subscribeService != null) {
            subscribeService.shutdown();
            subscribeService = null;
        }
    }

}
