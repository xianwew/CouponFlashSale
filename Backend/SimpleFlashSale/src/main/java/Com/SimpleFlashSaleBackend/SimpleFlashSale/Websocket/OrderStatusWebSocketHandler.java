package Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.io.IOException;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Component
public class OrderStatusWebSocketHandler extends TextWebSocketHandler {
    private static final Logger logger = LoggerFactory.getLogger(OrderStatusWebSocketHandler.class);
    private final ConcurrentMap<String, WebSocketSession> sessions = new ConcurrentHashMap<>();

    @Override
    public void afterConnectionEstablished(WebSocketSession session) {
        String sessionId = session.getId();
        sessions.put(sessionId, session);
        logger.info("✅ WebSocket connection established: {}", sessionId);
        logger.info("🔗 Current active WebSocket sessions: {}", sessions.size());
    }

    @Override
    protected void handleTextMessage(WebSocketSession session, TextMessage message) {
        String payload = message.getPayload();
        logger.info("📩 Received message from session {}: {}", session.getId(), payload);

        // Example: Log the current active sessions
        logger.info("🔗 Active WebSocket sessions: {}", sessions.size());
    }

    @Override
    public void handleTransportError(WebSocketSession session, Throwable exception) {
        logger.error("⚠️ WebSocket transport error in session {}: {}", session.getId(), exception.getMessage(), exception);
    }

    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) {
        String sessionId = session.getId();
        sessions.remove(sessionId);
        logger.info("🔌 WebSocket connection closed: {}, Reason: {}", sessionId, status);
        logger.info("🔗 Remaining active WebSocket sessions: {}", sessions.size());
    }

    public void sendOrderUpdate(String orderId, String message) {
        logger.info("📢 Sending WebSocket update to all clients: Order ID: {}, Message: {}", orderId, message);
        logger.info("🔗 Active WebSocket sessions before sending: {}", sessions.size());

        for (WebSocketSession session : sessions.values()) {
            if (session.isOpen()) {
                try {
                    session.sendMessage(new TextMessage("Order " + orderId + " update: " + message));
                    logger.info("✅ WebSocket message sent to session: {}", session.getId());
                } catch (IOException e) {
                    logger.error("❌ Failed to send WebSocket message to: {}. Error: {}", session.getId(), e.getMessage(), e);
                }
            } else {
                logger.warn("⚠️ Skipping closed WebSocket session: {}", session.getId());
            }
        }
    }
}