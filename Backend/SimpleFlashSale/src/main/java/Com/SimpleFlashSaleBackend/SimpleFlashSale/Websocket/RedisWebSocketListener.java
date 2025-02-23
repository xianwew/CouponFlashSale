package Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Service.PaymentService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.redis.connection.Message;
import org.springframework.data.redis.connection.MessageListener;
import org.springframework.stereotype.Component;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

@Component
public class RedisWebSocketListener implements MessageListener {

    private final OrderStatusWebSocketHandler webSocketHandler;

    private static final Logger logger = LoggerFactory.getLogger(RedisWebSocketListener.class);

    public RedisWebSocketListener(OrderStatusWebSocketHandler webSocketHandler) {
        this.webSocketHandler = webSocketHandler;
    }

    @Override
    public void onMessage(Message message, byte[] pattern) {
        try {
            String payload = new String(message.getBody());
            ObjectMapper mapper = new ObjectMapper();
            JsonNode jsonNode = mapper.readTree(payload);

            String serverId = jsonNode.get("serverId").asText();
            String orderId = jsonNode.get("orderId").asText();
            String status = jsonNode.get("status").asText();

            logger.info("Incoming Websocket update...");
            if (ServerIdGenerator.getServerId().equals(serverId)) {
                webSocketHandler.sendOrderUpdate(orderId, status);
                logger.info("✅ WebSocket update listened and sent for Order ID: {}, status: {}", orderId, status);
            }

        } catch (Exception e) {
            logger.info("⚠️ Error processing Redis message: " + e.getMessage());
        }
    }
}