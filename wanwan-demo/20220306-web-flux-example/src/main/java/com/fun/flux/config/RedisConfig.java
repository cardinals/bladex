package com.fun.flux.config;

import com.fun.flux.entity.UserVO;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.ReactiveRedisConnectionFactory;
import org.springframework.data.redis.core.ReactiveRedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;

/**
 * @author wanwan 2022/3/9
 */
@Configuration
public class RedisConfig {

	@Bean
	public ReactiveRedisTemplate<String, Object> commonRedisTemplate(ReactiveRedisConnectionFactory factory) {
		RedisSerializationContext<String, Object> serializationContext =
			RedisSerializationContext.<String, Object>newSerializationContext(RedisSerializer.string())
				.value(RedisSerializer.json()) // 创建通用的 GenericJackson2JsonRedisSerializer 作为序列化
				.build();
		return new ReactiveRedisTemplate<>(factory, serializationContext);
	}

	@Bean
	public ReactiveRedisTemplate<String, UserVO> userRedisTemplate(ReactiveRedisConnectionFactory factory) {
		RedisSerializationContext<String, UserVO> serializationContext =
			RedisSerializationContext.<String, UserVO>newSerializationContext(RedisSerializer.string())
				.value(new Jackson2JsonRedisSerializer<>(UserVO.class)) // 创建专属 UserCacheObject 的 Jackson2JsonRedisSerializer 作为序列化
				.build();
		return new ReactiveRedisTemplate<>(factory, serializationContext);
	}
}
