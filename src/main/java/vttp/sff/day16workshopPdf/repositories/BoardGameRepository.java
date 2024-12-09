package vttp.sff.day16workshopPdf.repositories;

import java.util.Optional;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Repository;

import jakarta.json.JsonObject;

@Repository
public class BoardGameRepository {

    @Autowired
    @Qualifier("redis-string")
    private RedisTemplate<String, String> redisTemplate;

    // hset redis_key gameId gameAsJson
    public void storeJson(String redisKey, JsonObject boardGameAsJsonObject) {
        redisTemplate.opsForHash().put(redisKey, String.valueOf(boardGameAsJsonObject.getInt("gid")), boardGameAsJsonObject.toString());
    }

    // hget redis_key gameId gameAsJson
    public Optional<String> getBoardGameById(String key, String id) {
        
        HashOperations<String, String, String> hashOps = redisTemplate.opsForHash();

        if (!hashOps.hasKey(key, id))
            return Optional.empty();

        return Optional.of(hashOps.get(key, id));
    }

    public boolean boardGameIdExist(String key, String id) {
        return redisTemplate.opsForHash().hasKey(key, id);
    }
}
