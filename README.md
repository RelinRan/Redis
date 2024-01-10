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
连接客户端
```
Redis redis = new Redis("127.0.0.1", 6379);
String auth = redis.auth("xxx");
```




