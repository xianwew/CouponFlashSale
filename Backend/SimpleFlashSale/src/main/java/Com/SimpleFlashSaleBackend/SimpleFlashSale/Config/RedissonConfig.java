package Com.SimpleFlashSaleBackend.SimpleFlashSale.Config;

import org.redisson.Redisson;
import org.redisson.api.RedissonClient;
import org.redisson.config.Config;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class RedissonConfig {

    @Value("${redis.url}")
    private String redisURI;

    @Bean
    public RedissonClient redissonClient() {
        Config config = new Config();
        config.useSingleServer()
                .setAddress(redisURI)
                .setConnectionMinimumIdleSize(10)
                .setConnectionPoolSize(50);

        return Redisson.create(config);
    }
}
