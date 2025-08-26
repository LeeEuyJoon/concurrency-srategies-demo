package study.concurrencyproblem.repository;

import java.time.Duration;
import java.util.Collections;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.stereotype.Component;

@Component
public class RedisLockRepository {

	private final StringRedisTemplate redis;
	private final DefaultRedisScript<Long> unlockScript;

	public RedisLockRepository(StringRedisTemplate redis) {
		this.redis = redis;

		this.unlockScript = new DefaultRedisScript<>();
		this.unlockScript.setScriptText(
			"if redis.call('GET', KEYS[1]) == ARGV[1] then " +
				"  return redis.call('DEL', KEYS[1]) " +
				"else return 0 end"
		);
		this.unlockScript.setResultType(Long.class);
	}

	/** SET key token NX PX ttl */
	public boolean tryLock(String key, String token, Duration ttl) {
		Boolean ok = redis.opsForValue().setIfAbsent(key, token, ttl);
		return Boolean.TRUE.equals(ok);
	}

	/** Lua로 소유권 검증 후 해제 */
	public boolean unlock(String key, String token) {
		Long res = redis.execute(unlockScript, Collections.singletonList(key), token);
		return res != null && res == 1L;
	}

	/** 네임스페이스 유틸 */
	public static String lockKeyForAccount(Long id) {
		return "lock:acc:" + id;
	}
}
