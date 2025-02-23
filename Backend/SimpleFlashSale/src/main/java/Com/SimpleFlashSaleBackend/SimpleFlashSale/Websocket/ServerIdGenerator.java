package Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket;

import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
public class ServerIdGenerator {

    private static final String serverId = UUID.randomUUID().toString();

    public static String getServerId() {
        if (serverId == null) {
            throw new IllegalStateException("Server ID has not been initialized!");
        }
        return serverId;
    }
}