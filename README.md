#### Redis客户端

Java-微型redis客户端工具

#### 资源

|名字|资源|
|-|-|
|Jar|[下载](https://github.com/RelinRan/Redis/tree/main/jar)|
|JavaDoc|[查看](https://github.com/RelinRan/Redis/tree/main/doc)|
|GitHub |[查看](https://github.com/Redis/Redis)|
|Gitee|[查看](https://gitee.com/relin/Redis)|

#### Maven

1.build.grade | setting.grade

```
repositories {
	...
	maven { url 'https://jitpack.io' }
}
```

2./app/build.grade

```
dependencies {
	implementation 'com.github.RelinRan:Redis:2024.01.10.3'
}
```
#### 使用
|修饰符和类型| 方法 |说明|
|-|-|-|
|String| acl(String... subcommands) |Redis 从版本 6.0 开始引入的功能，用于管理和控制对 Redis 数据库的访问权限。|
|Long| append(String key, String value) |指定键的值追加一个字符串|
|String| asking() |在事务模式下执行指定命令的命令|
|String| auth(String password)| 授权 |
|String | auth(String user, String password)| 授权 |
|String | bgrewriteaof()|用于在后台异步重写当前的 AOF（Append Only File）文件，它会生成一个新的AOF文件来替代当前的AOF文件。|
|String |bgsave()|在后台异步保存当前数据库的数据到硬盘上的 RDB 文件中|
|List<String>| bitfield(String key, String... subcommands)| 对字符串（String）类型的位（bit）操作的命令。|
|Long| bitop(String operation, String destkey, String... key)| 对一个或多个位图进行位运算，并将结果保存到指定的目标位图中。|
|Long| bitpos(String key)| 用于统计字符串值中 位（bit） 的数量|
|Long| bitpos(String key, int bit)|BITPOS 命令用于查找指定键的字符串值的位图中，从左到右第一个设置为给定值的位的偏移量。|
|Long| bitpos(String key, int start, int end)| 用于统计字符串值中 位（bit） 的数量|
|Long| bitpos(String key, int bit, int start, int end)| BITPOS 命令用于查找指定键的字符串值的位图中，从左到右第一个设置为给定值的位的偏移量。|
|String| blpop(String[] key, int timeout)| 从一个或多个列表的最左端阻塞式地获取并删除第一个元素|
|String| brpop(String[] key, int timeout)| 从一个或多个列表中阻塞式地获取并删除最后一个元素|
|String| brpoplpush(String source, String destination, int timeout)| 原子性地从一个列表的尾部弹出一个元素，并将该元素推入到另一个列表的头部。|
|List<String>|bzpopmax(String[] key, long timeout)| Redis 的 BZPOPMAX 命令用于在阻塞列表中弹出具有最高分值的元素。|
|List<String>| bzpopmin(String[] key, long timeout)| Redis 的 BZPOPMIN 命令用于在阻塞列表中弹出具有最低分值的元素。|
|List<String>| channels(String pattern)| 列出当前被订阅的频道|
|String| client(String... options)| 用于查看和管理客户端连接到 Redis 服务器的情况。|
|void| close()|关闭服务|
|String| cluster(String option)| 用于管理和操作集群|
|String| config(String... options)| CONFIG 命令用于配置 Redis 服务器的运行时参数|
|Long| dbSize()| 查询当前数据库中键的数量|
|String| debug(String... options)| 提供了一些用于调试和诊断的子命令，主要用于内部使用和开发调试。|
|Long| decr(String key)| 获取递减值|
|Long| decrby(String key, Long decrement)| 将指定键的值按给定的减量进行减法运算|
|Long| del(String... key)| 删除指定key|
|String| dump(String key)| 序列化给定 key，并返回序列化值。|
|String| echo(String message)| ECHO 命令用于返回输入的参数。|
|String| eval(String script, int numkeys, String[] key, String[] arg)| 用于执行 Lua 脚本。|
|String| evalsha(String sha1, int numkeys, String[] key, String[] arg)| 用于执行存储在 Redis 服务器中的 Lua 脚本的 SHA1 校验和|
|Long| exists(String... keys)| 是否存在key|
|Long| expire(String key, int seconds)| 设置指定key的过期时间（以秒为单位）|
|Long| expireAt(String key, Long timestamp)| 设置指定key的过期时间|
|String| flushAll(boolean async)| 删除 Redis 中的所有数据库中的所有键|
|Long| flushDB(boolean async)| 删除当前数据库中的所有键|
|Long| geoadd(String key, double[] longitude, double[] latitude, String[] member)| 将经度和纬度坐标存储在一个给定的地理空间索引中。|
|String| geodist(String key, String member1, String member2)| 计算地理位置的距离|
|List<String>| geohash(String key, String... member)| 获取地理位置的 GeoHash 值|
|List<String>| geopos(String key, String... member)| 获取地理位置的经度和纬度信息|
|List<String>| georadius(String key, double longitude, double latitude, int radius, String unit, String[] options)| 根据给定的中心位置和半径范围，查询符合条件的地理位置成员|
|List<String>| georadius_ro(String key, double longitude, double latitude, int radius, String unit, String[] options)| 只读版本的 GEORADIUS 命令，用于在不进行写操作的情况下执行地理位置的范围查询|
|List<String>| georadiusbymember(String key, String member, int radius, String unit, String[] options) |通过给定的地理位置成员名，查询符合指定半径范围内的其他地理位置成员|
|List<String>| georadiusbymember_ro(String key, String member, int radius, String unit, String[] options)| 一个只读版本的 GEORADIUSBYMEMBER 命令，用于在不进行写操作的情况下执行以地理位置成员为中心的范围查询|
|String| get(String key)| 获取值|
|Long| getbit(String key, int offset)| 获取指定键的字符串值的位图中偏移量上的位的值。|
|String| getDel(String key)| 获取删除key|
|String| getRange(String key, int start, int end)| Redis 2.0 版本中的一个字符串命令，用于获取指定字符串的子串|
|String| getSet(String key, String value)| 获取指定键的值，并将新值设置为该键的值|
|Long| hdel(String key, String... field)| 用于删除指定哈希表中一个或多个字段的值, 如果指定的哈希表、或者指定的字段不存在于哈希表中，那么 HDEL 命令将返回 0。|
|Long| hexists(String key, String field)| 检查指定的哈希表中是否存在指定的字段|
|String| hget(String key, String field)| 获取指定key的哈希表中字段的值|
|Map<String,String>| hgetAll(String key)| 获取指定key的哈希表中所有的字段和值|
|Long| hincrby(String key, String field, int increment)| Redis 的一个哈希（Hash）命令，用于将指定字段的值按给定增量递增|
|String| hincrbyfloat(String key, String field, float increment)| 在哈希数据结构中对存储浮点数的字段进行增减操作的命令|
|List<String>| hkeys(String key)| 获取指定哈希表中所有的字段名|
|Long| hlen(String key)| 获取指定哈希表中字段的数量|
|Map<String,String>| hmget(String key, String... field)| 从指定的哈希表中获取多个字段的值|
|String| hmset(String key, Map<String,String> map)| 将多个字段和值同时设置到指定key的哈希表中|
|Map<String,String>| hRandField(String key, int count)| Redis 6.2 版本及以上可用,从指定的哈希表中随机返回一个字段以及它的值|
|List<String>| hscan(String key, int cursor)| 用于遍历哈希表的命令，它用于迭代访问哈希表中的键值对|
|List<String>| hscan(String key, int cursor, String pattern, int count)| 用于遍历哈希表的命令，它用于迭代访问哈希表中的键值对|
|Long| hset(String key, String field, String value)| Redis 2.0 将给定字段和值存储在指定的哈希表中|
|Long| hsetnx(String key, String field, String value)| 只在给定字段不存在时，将字段及其值存储在指定的哈希表中|
|Long| hstrlen(String key, String field)| 获取哈希表（Hash）中指定字段的值的长度|
|List<String>| hvals(String key)| 获取指定哈希表中所有的字段值|
|Long| incr(String key)| 获取递增值|
|Long| incrby(String key, Long decrement)| 将指定键的值按给定的增量进行加法运算|
|Long| incrbyfloat(String key, float increment)| 将键存储的值以浮点数进行增加。|
|String| info()| 用于获取关于 Redis 服务器的各种信息和统计数据。|
|boolean| isEmpty(String value)| 是否为空|
|List<byte[]>| keys(String pattern)| 根据指定的pattern模式返回匹配的key列表|
|Long| lastsave()| 获取最近一次成功持久化数据库的时间戳。|
|String| lindex(String key, int index)| 通过索引获取列表中的元素，列表的起始索引是 0。|
|Long| linsert(String key, boolean before, String pivot, String value)| 在列表（list）中指定元素的前后位置插入一个新元素|
|Long| llen(String key)| 获取指定列表的长度（即包含的元素个数）|
|String| lpop(String key)| 移除并返回列表的头部元素|
|Long| lpos(String key, String element, int rank)| Redis 6.0 或更高版本,在列表（List）中查找指定元素的位置。|
|Long| lpush(String key, String value)| 将一个或多个值插入到列表的头部|
|Long| lpush(String key, String... values)| 将一个或多个值插入到列表的头部|
|Long| lpushx(String key, String value)| 用于将一个或多个值插入到已存在的列表的头部。|
|List<String>| lrange(String key, int start, int stop)| 获取指定列表中指定范围内的元素|
|Long| lrem(String key, int count, String value)| 从列表中移除指定数量的元素|
|String| lset(String key, int index, String value)| 设置列表指定索引处的元素的值|
|String| ltrim(String key, int start, int stop)| Redis 的一个列表（List）命令，修剪（trim）指定列表，只保留列表中指定范围内的元素，而移除其他元素|
|String| memory(String... options)| 管理内存相关操作的命令集合|
|List<String>| mget(String... key)| 获取多个键的值|
|String| migrate(String host, int port, String key, int destinationDb, String timeout, boolean copy, boolean replace)| 将指定的键从一个 Redis 实例迁移到另一个 Redis 实例。|
|String| module(String... options)| 管理和加载 Redis 模块的命令。|
|String| monitor()| 用于实时监视客户端发送到 Redis 服务器的命令请求，并将这些请求实时输出到服务器的日志中。|
|Long| move(String key, int index)| 将指定的键从当前数据库移动到另一个数据库|
|String| mset(Map<String,String> map)| 同时设置多个键-值对|
|String| msetnx(Map<String,String> map)| 用于同时设置多个键-值对，但仅在所有指定的键都不存在时才进行设置|
|Long| numpat()| 获取被订阅到的模式的数量|
|Map<String,String>| numsub(String channel)| 获取指定频道的订阅者数量|
|Long| persist(String key)| 用于移除给定键（key）的过期时间，使其成为永久有效的键|
|Long| pexpire(String key, long milliseconds)| 用于设置键的过期时间，单位为毫秒。|
|Long| pexpireat(String key, long millisecondsTimestamp)| 用于设置键的过期时间戳，单位为毫秒。|
|Long| pfadd(String key, String... element)| 一种特殊数据类型 HyperLogLog 的命令，用于将一个或多个元素添加到 HyperLogLog 数据结构中|
|Long| pfcount(String... key)| 获取 HyperLogLog 数据结构中近似不重复元素数量的命令|
|String| pfmerge(String destkey, String... sourceKey)| 合并多个 HyperLogLog 数据结构，生成一个新的 HyperLogLog 数据结构|
|String| ping()| ping服务器是否可用，返回PONG即可用|
|String| ping(String message)| ping服务器是否可用|
|socket.redis.Protocol| protocol()| 协议对象|
|Long| psetex(String key, long milliseconds, String value)| 用于设置指定键的值，并在指定的毫秒数后自动删除该键|。
|void| psubscribe(String[] patterns, socket.redis.OnSubscribeListener onSubscribeListener)| 订阅一个或多个与给定模式匹配的频道的命令|
|Long| pttl(String key)| 用于获取键的剩余过期时间，以毫秒为单位。|
|Long| publish(String channel, String message)| 发布信息|
|void| punsubscribe(String[] patterns, socket.redis.OnSubscribeListener onSubscribeListener)| 用于取消订阅指定模式的频道的命令|
|String| quit()| 关闭服务器连接|
|String| randomKey()| 随机获取数据库中的一个键|
|byte[]| read()| 读取单个|
|byte[]| read(int capacity)| 读取单个|
|String| readonly()| 用于将 Redis 实例设置为只读模式|
|String| readwrite()| 用于取消将 Redis 实例设置为只读模式，使其重新变为读写模式|
|String| rename(String key, String name)| 重命名一个键|
|String| renamenx(String key, String name)| 在新的键名不存在时重命名一个键|
|String| restore(String key, long ttl, String serializedValue, boolean replace)| 用于将经 DUMP 命令序列化得到的值反序列化，并将它恢复到 Redis 中特定的键|
|List<String>| role()| 获取 Redis 服务器的角色 服务器角色的字符串表示，可以是 “master”、“slave” 或者 “single”。|
|String| rpop(String key)| 移除并返回列表的尾部元素|
|String| rpoplpush(String source, String destination)| 将列表 source 中的最后一个元素弹出，并将其插入到列表 destination 的最前面， 形成一个元素从 source 到destination 的“原子性”操作。|
|Long| rpush(String key, String value)| 将一个或多个值插入到已存在的列表的尾部|
|Long| rpush(String key, String... values)| 将一个或多个值插入到已存在的列表的尾部|
|Long| rpushx(String key, String value)| 将一个或多个值插入到已存在的列表的尾部。|
|Long| sadd(String key, String... member)| 向集合中添加一个或多个元素|
|String| save()| 将当前数据库的数据保存到硬盘上的 RDB 文件中|
|List<String>| scan(int cursor)| 用于遍历数据库中的键的命令，它支持在不阻塞服务器的情况下逐步遍历数据库中的键空间|
|List<String>| scan(int cursor, String pattern, int count)| 用于遍历数据库中的键的命令，它支持在不阻塞服务器的情况下逐步遍历数据库中的键空间|
|Long| scard(String key)| 获取集合中的成员数量|
|String| script(String... script)| 在 Redis 服务器上执行 Lua 脚本|
|List<String>| sdiff(String... key)| 用于求两个集合的差集，即从第一个集合中移除在其他集合中存在的元素|
|Long| sdiffstore(String destination, String... key)| 用于求两个或多个集合的差集，并将结果存储到一个新的集合中|
|String| select(int index)| 切换到指定的数据库|
|void| send(byte[] data)| 发送|
|void| send(socket.redis.Command command, byte[]... args)| 发送信息|
|void| send(socket.redis.Command command, String... args)| 发送信息|
|String| sentinel(String... options)| 用于管理和监控 Redis Sentinel 守护程序的命令，这些命令可以用来查询监控信息、配置参数、执行故障转移和其他管理任务|
|String| set(String key, String value)| 获取值|
|Long| setbit(String key, int offset, int value)| 用于设置指定键的字符串值的位图中偏移量上的位的值。|
|String| setex(String key, String value, int seconds)| 设置一个键-值对，并指定键的过期时间
|Long| setnx(String key, String value)| 设置一个键的值，仅当该键不存在时才进行设置|
|Long| setrange(String key, int offset, String value)| 将指定键的字符串值从指定偏移量开始的部分替换为另一个字符串|
|String| shutdown()| 关闭当前运行的 Redis 服务器|
|List<String>| sinter(String... key)| 用于求两个或多个集合的交集|
|Long| sinterstore(String destination, String... key)| 用于求两个或多个集合的交集，并将结果存储到一个新的集合中|
|boolean| sismember(String key, String member)| 判断一个元素是否属于集合|
|String| slaveof(String masterip, int masterport)| 将当前 Redis 服务器设置为一个指定主服务器的从服务器（slave）|
|String| slaveofnoone()| 将当前 Redis 服务器从从服务器恢复为独立服务器|
|String| slowlog(String subcommand, String argument)| 查看 Redis 服务器的慢日志信息|
|List<String>| smembers(String key)| 用于返回集合中的所有元素|
|Long| smove(String source, String destination, String member)| 将集合中的一个元素移动到另一个集合中|
|String| sort(String key)| 对列表、集合或有序集合中的元素进行排序|
|String| sort(String key, String pattern)| 对列表、集合或有序集合中的元素进行排序|
|List<String>| spop(String key, int count)| 从集合中随机地移除并返回一个或多个元素|
|List<String>| srandmember(String key, int count)| 用于从集合中随机返回一个元素|
|Long| srme(String key, String... member)| 从集合中删除一个或多个元素|
|List<String>| sscan(String key, int cursor)| 遍历集合（Set）中的元素的命令，它支持在不阻塞服务器的情况下逐步遍历集合中的元素|
|List<String>| sscan(String key, int cursor, String pattern, int count)| 遍历集合（Set）中的元素的命令，它支持在不阻塞服务器的情况下逐步遍历集合中的元素|
|Long| strlen(String key)| 获取存储在指定键（key）的字符串值的长度|
|void| subscribe(String[] channels, socket.redis.OnSubscribeListener onSubscribeListener)| 订阅|
|void| subscribe(String channel, socket.redis.OnSubscribeListener onSubscribeListener)| 订阅|
|String| substr(String key, int start, int end)| Redis 1.0 版本中的一个字符串命令，用于获取指定字符串的子串|
|List<String>| sunion(String... key)| 用于求两个或多个集合的并集|
|Long| sunionstore(String destination, String... key)| 用于求两个或多个集合的并集，并将结果存储到一个新的集合中|
|String| swapdb(int index1, int index2)| 交换两个数据库的数据,默认情况下有 16 个数据库（编号为 0 到 15）|
|List<String>| time()| 获取服务器当前的时间，返回一个包含两个元素的数组，分别是当前时间的秒数和微秒数|
|Long| ttl(String key)| 获取指定key的剩余过期时间（以秒为单位）|
|String| type(String key)| 获取指定键存储的值的数据类型|
|Long| unlink(String... key)| 异步删除指定的键|
|String| unsubscribe()| 取消所有订阅|
|String| unsubscribe(String channel)| 取消订阅|
|String| unsubscribe(String[] channels)| 取消订阅|
|Long| wait(int numreplicas, long timeout)| 等待（阻塞）直到指定数量的从节点（replica）进行复制操作并确认复制的数据量达到指定的要求|
|Long| xack(String key, String group, String... id)| 向 Redis Streams 数据结构发送确认消息已经成功处理的信号 它标记一个或多个消息已经被消费者处理完成，以便 streams 中的消息可以被清理或进一步处理。|
|String| xadd(String key, String id, Map<String,String> value)| 向流数据结构中添加条目|
|List<String>| xclaim(String stream, String group, String consumer, String time, String id, String idle, String retrycount, boolean force, boolean justid)| 用于将消息从一个消费者组的待处理状态转移到另一个消费者组或消费者|
|String| xgroup(String... subcommand)| 管理消费者组的命令。|
|String| xgroupcreate(String stream, String group, String id)| 创建一个消费者组|
|Long| xgroupdestroy(String stream, String group)| 销毁一个消费者组|
|String| xgroupsetid(String stream, String group, String id)| 设置消费者组中某个消费者的待处理消息最新 ID|
|String| xgroupsetid(String stream, String group, String consumer, boolean noack, boolean justid)| 设置消费者组中某个消费者的消费者挂起状态|
|List<String>| xinfo(String... subcommands)| XINFO 命令用于获取 Redis Stream（流）的信息。|
|Long| xlen(String streamName)| 获取指定流中的条目数量|
|List<String>| xpending(String stream, String group)| 用于获取消费者组中待处理消息的信息。|
|List<String>| xpending(String stream, String group, String start, String end, int count, String consumer)| 用于获取消费者组中待处理消息的信息。|
|Map<String,String>| xrange(String key, String start, String end, int count)| 获取范围内的流数据条目|
|List<String>| xread(int count, long milliseconds, String[] key, String[] id)| 用于读取和消费 Redis Streams 数据结构中的消息,它可以按照消息流的ID来读取特定范围内的消息，并以数组的形式返回这些消息|
|List<String>| xread(String[] key, String[] id)| 用于读取和消费 Redis Streams 数据结构中的消息,它可以按照消息流的ID来读取特定范围内的消息，并以数组的形式返回这些消息|
|List<String>| xreadgroup(String group, String consumer, int count, long milliseconds, String[] key, String[] id)| 从 Redis Streams 数据结构中以消费者群组的形式读取消息的命令|
|List<String>| xreadgroup(String group, String consumer, String[] key, String[] id)| 从 Redis Streams 数据结构中以消费者群组的形式读取消息的命令|
|List<String>| xrevrange(String streamName, String endId, String startId, int count)| 按照逆序获取指定范围的消息流中的元素|
|Long| xtrim(String streamName, String condition, int minid)| 对流数据结构进行修剪（删除）操作|
|Long| zadd(String key, Map<Integer,String> value)| 向有序集合（sorted set）中添加一个或多个成员的命令|
|Long| zcard(String key)| 获取有序集合中成员的数量|
|Long| zcount(String key, int min, int max)| 获取有序集合（Sorted Set）中指定分数范围内的成员数量的命令|
|String| zincrby(String key, int increment, String member)| 对有序集合中指定成员的分值进行增加或减少操作|
|Long| zinterstore(String destination, int numkeys, String[] key, int[] weights, String aggregate)| 用于计算多个有序集合的交集，并将结果存储在一个新的有序集合中的命令|
|Long| zlexcount(String key, String min, String max)| 计算有序集合中指定字典区间范围内的成员数量|
|List<String>| zpopmax(String key, int count)| 删除并获取有序集合中分值最大的成员|
|List<String>| zpopmin(String key, int count)| 删除并获取有序集合中分值最小的成员|
|List<String>| zrange(String key, int start, int stop)| 获取有序集合中的一定范围内的成员|
|List<String>| zrangebylex(String key, String min, String max)| 获取有序集合中指定字典区间范围内的成员列表|
|List<String>| zrangebyscore(String key, int min, int max)| 根据分数范围获取有序集合（Sorted Set）中的成员的命令|
|Long| zrank(String key, String member)| 获取有序集合中指定成员的排名（即索引位置）|
|Long| zrem(String key, String... member)| 从有序集合中移除一个或多个指定的成员|
|List<String>| zremrangebylex(String key, String min, String max)| 移除有序集合（sorted set）中按字典区间（lexicographical range）排列的成员。|
|Long| zremrangebyrank(String key, int start, int stop)| 根据排名范围删除有序集合（Sorted Set）中的成员的命令|
|Long| zremrangebyscore(String key, int min, int max)| 根据分数范围删除有序集合（Sorted Set）中的成员的命令|
|List<String>| zrevrange(String key, int start, int stop)| 按照逆序获取有序集合中指定排名范围内的成员|
|List<String>| zrevrangebylex(String key, String max, String min)| 获取有序集合中指定字典区间范围内的成员列表，但是按照逆序排列返回。|
|List<String>| zrevrangebyscore(String key, int max, int min)| 根据分数范围从大到小获取有序集合（Sorted Set）中的成员的命令|
|Long| zrevrank(String key, String member)| 获取有序集合中指定成员的逆序排名（即从大到小的排名）。|
|List<String>| zscan(String key, int cursor)| 遍历有序集合（Sorted Set）中的成员的命令，它支持在不阻塞服务器的情况下逐步遍历有序集合中的成员|
|List<String>| zscan(String key, int cursor, String pattern, int count)| 遍历有序集合（Sorted Set）中的成员的命令，它支持在不阻塞服务器的情况下逐步遍历有序集合中的成员|
|String| zscore(String key, String member)| 获取有序集合中指定成员的分值|
|Long| zunionstore(String destination, int numkeys, String[] key, int[] weights, String aggregate)| 计算多个有序集合的并集，并将结果存储在一个新的有序集合中|