package Com.SimpleFlashSaleBackend.SimpleFlashSale.Config;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket.RedisWebSocketListener;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket.ServerIdGenerator;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.listener.PatternTopic;
import org.springframework.data.redis.listener.RedisMessageListenerContainer;

@Configuration
public class RedisConfig {

    private static final Logger logger = LoggerFactory.getLogger(RedisConfig.class);

    @Bean
    public RedisMessageListenerContainer redisContainer(RedisConnectionFactory connectionFactory,
                                                        RedisWebSocketListener listener) {
        RedisMessageListenerContainer container = new RedisMessageListenerContainer();
        container.setConnectionFactory(connectionFactory);

        String serverId = ServerIdGenerator.getServerId(); // ✅ Get correct Server ID
        String serverIdChannel = "websocket-updates-" + serverId;

        container.addMessageListener(listener, new PatternTopic(serverIdChannel));

        logger.info("✅ Subscribed to Redis channel: " + serverIdChannel);

        return container;
    }
}
