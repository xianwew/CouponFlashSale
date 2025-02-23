package Com.SimpleFlashSaleBackend.SimpleFlashSale.Config;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.CouponDTO;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Configuration
public class CaffeineConfig {

    @Bean
    public com.github.benmanes.caffeine.cache.Cache<String, List<CouponDTO>> couponCache() {
        return Caffeine.newBuilder()
                .expireAfterWrite(30, TimeUnit.SECONDS) // Expiration time of 30 seconds
                .maximumSize(100) // Max cache size (adjust as needed)
                .build();
    }
}
