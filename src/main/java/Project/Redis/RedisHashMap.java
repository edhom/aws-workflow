package Project.Redis;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import io.lettuce.core.*;
import io.lettuce.core.api.StatefulRedisConnection;
import io.lettuce.core.api.sync.RedisCommands;


public abstract class RedisHashMap implements Map<String, String> {


    private RedisClient redisClient;
    private StatefulRedisConnection<String, String> connection;
    private RedisCommands<String, String> syncCommands;

    public RedisHashMap(String connectionString){
        redisClient = RedisClient.create(connectionString);
        connection = redisClient.connect();
        syncCommands = connection.sync();
    }

    @Override
    public String get(Object key) {
        return syncCommands.get((String) key);
    }

    @Override
    public String put(String key, String value) {
        return syncCommands.set(key, value);
    }



}
