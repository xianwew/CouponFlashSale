package Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.*;

@Configuration
@EnableWebSocket
public class WebSocketConfig implements WebSocketConfigurer {

    private final OrderStatusWebSocketHandler orderStatusWebSocketHandler;

    public WebSocketConfig(OrderStatusWebSocketHandler orderStatusWebSocketHandler) {
        this.orderStatusWebSocketHandler = orderStatusWebSocketHandler;
    }

    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {
        registry.addHandler(orderStatusWebSocketHandler, "/ws/orders/*")
                .setAllowedOrigins("*");
    }
}