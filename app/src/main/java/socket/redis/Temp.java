package socket.redis;

public class Temp {

    public static void main(String[] args) {
        Redis redis = new Redis("127.0.0.1", 6379);
        String auth = redis.auth("123456");
    }

}
