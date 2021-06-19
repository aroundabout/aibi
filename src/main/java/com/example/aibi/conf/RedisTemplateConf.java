package com.example.aibi.conf;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Lazy;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.GenericJackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.GenericToStringSerializer;

/**
 * redis配置
 * <p>
 * 集群下启动session共享，需打开@EnableRedisHttpSession
 * <p>
 * 单机下不需要
 *
 * @author jorian
 * <p>
 * 2019年3月10日
 */
//@EnableRedisHttpSession
@Configuration
public class RedisTemplateConf {

    @SuppressWarnings({"unchecked", "rawtypes"})
    @Bean("redisTemplate")
    public RedisTemplate redisTemplate(@Lazy RedisConnectionFactory connectionFactory) {
        RedisTemplate redis = new RedisTemplate();
        GenericToStringSerializer keySerializer = new GenericToStringSerializer(String.class);
        //key序列化方式
        redis.setKeySerializer(keySerializer);
        //哈希key序列化方式
        redis.setHashKeySerializer(keySerializer);
        GenericJackson2JsonRedisSerializer valueSerializer = new GenericJackson2JsonRedisSerializer();
        //值序列化方式
        redis.setValueSerializer(valueSerializer);
        //哈希值序列化方式
        redis.setHashValueSerializer(valueSerializer);
        //连接器工厂
        redis.setConnectionFactory(connectionFactory);
        return redis;
    }

}