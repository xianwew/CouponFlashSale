package Com.SimpleFlashSaleBackend.SimpleFlashSale.Config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

import java.util.concurrent.Executor;

@Configuration
@EnableAsync
public class AsyncConfig {

    @Bean(name = "paymentExecutor")
    public Executor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(5);  // Min 5 threads
        executor.setMaxPoolSize(10);  // Max 10 threads
        executor.setQueueCapacity(50);
        executor.setThreadNamePrefix("PaymentThread-");
        executor.initialize();
        return executor;
    }
}