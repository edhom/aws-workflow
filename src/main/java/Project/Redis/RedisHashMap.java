package Project.Redis;

import java.util.List;
import io.lettuce.core.cluster.RedisClusterClient;
import io.lettuce.core.cluster.api.StatefulRedisClusterConnection;
import io.lettuce.core.cluster.api.sync.RedisAdvancedClusterCommands;

public class RedisHashMap {

    private RedisClusterClient redisClient;
    private StatefulRedisClusterConnection<String, String> connection;
    private RedisAdvancedClusterCommands<String, String> syncCommands;

    public RedisHashMap(String connectionString){
        String uri = "redis://" + connectionString;
        redisClient = RedisClusterClient.create(uri);
        connection = redisClient.connect();
        syncCommands = connection.sync();
        System.out.println("Redis Client creation successful");
    }

    public String get(Object key) {
        return syncCommands.get((String) key);
    }

    public String put(String key, String value) {
        return syncCommands.set(key, value);
    }

    public List<String> getKeys() {
        return syncCommands.keys("*");
    }

    public void close() {
        connection.close();
        redisClient.shutdown();
    }

}
