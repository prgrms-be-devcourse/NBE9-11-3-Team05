//package com.team05.petmeeting.global.config.redis;
//
//import static org.assertj.core.api.Assertions.assertThat;
//
//import org.junit.jupiter.api.Test;
//import org.springframework.beans.factory.annotation.Autowired;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.data.redis.core.StringRedisTemplate;
//import org.springframework.test.context.ActiveProfiles;
//
//@SpringBootTest
//@ActiveProfiles("test")
//class RedisConfigTest {
//
//    @Autowired
//    private StringRedisTemplate redisTemplate;
//
//    @Test
//    void redisConnectionTest() {
//        // given
//        String key = "test:key";
//        String value = "hello redis";
//
//        // when
//        redisTemplate.opsForValue().set(key, value);
//        String result = redisTemplate.opsForValue().get(key);
//
//        // then
//        assertThat(result).isEqualTo(value);
//    }
//}