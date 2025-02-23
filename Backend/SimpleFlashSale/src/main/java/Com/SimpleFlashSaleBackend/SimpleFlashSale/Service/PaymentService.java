package Com.SimpleFlashSaleBackend.SimpleFlashSale.Service;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Websocket.OrderStatusWebSocketHandler;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.Coupon;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.User;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.UserCoupon;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Exception.ResourceNotFoundException;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository.CouponRepository;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository.UserCouponRepository;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository.UserRepository;
import org.redisson.api.RBucket;
import org.redisson.api.RScript;
import org.redisson.api.RedissonClient;
import org.redisson.client.codec.StringCodec;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class PaymentService {
    private final UserRepository userRepository;
    private final CouponRepository couponRepository;
    private final UserCouponRepository userCouponRepository;
    private final RedissonClient redissonClient;
    private final StringRedisTemplate redisTemplate;

    private static final String COUPON_CACHE_PREFIX = "SimpleFlashSale#coupon:";
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    public PaymentService(UserRepository userRepository, CouponRepository couponRepository,
                          UserCouponRepository userCouponRepository, RedissonClient redissonClient,
                          StringRedisTemplate redisTemplate) {
        this.userRepository = userRepository;
        this.couponRepository = couponRepository;
        this.userCouponRepository = userCouponRepository;
        this.redissonClient = redissonClient;
        this.redisTemplate = redisTemplate;
    }

    @KafkaListener(topics = "${kafka.topic.payment}", groupId = "payment-group")
    @Transactional
    public void listenForPayments(String message) {
        processPaymentAsync(message);
    }

    @Async
    public void processPaymentAsync(String message) {
        String[] data = message.split(",");
        UUID orderId = UUID.fromString(data[0]);
        String userId = data[1];
        Long couponId = Long.parseLong(data[2]);
        String serverId = data[3];

        try {
            logger.info("🛒 Processing payment for Order ID: {}", orderId);

            // Simulate a payment delay
            Thread.sleep(3000);

            User user = userRepository.findById(userId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + userId));
            Coupon coupon = couponRepository.findById(couponId)
                    .orElseThrow(() -> new ResourceNotFoundException("Coupon not found: " + couponId));

            if (user.getCredit() < coupon.getPrice()) {
                logger.error("❌ Payment failed for Order ID: {}, Insufficient credit", orderId);
                publishToRedis(serverId, orderId.toString(), "Payment failed: Insufficient credit.");

                // Restore coupon stock in Redis using Lua script
                restoreStockInRedis(userId, couponId);
                return;
            }

            // Deduct credit and save to MySQL
            user.setCredit(user.getCredit() - coupon.getPrice());
            userRepository.save(user);

            // ✅ Save UserCoupon with UUID as ID
            UserCoupon userCoupon = new UserCoupon();
            userCoupon.setId(orderId.toString());
            userCoupon.setUser(user);
            userCoupon.setCoupon(coupon);
            userCoupon.setPaymentSuccess(true);
            userCouponRepository.save(userCoupon);

            synchronized (this) {
                coupon.setQuantity(coupon.getQuantity() - 1);
                couponRepository.save(coupon);
            }

            logger.info("✅ Payment success for Order ID: {}", orderId);
            publishToRedis(serverId, orderId.toString(), "Payment successful!");

        } catch (Exception e) {
            logger.error("⚠️ Error processing payment: {}", e.getMessage());
            publishToRedis(serverId, orderId.toString(), "Payment processing error: " + e.getMessage());
        }
    }

    private void restoreStockInRedis(String userId, Long couponId) {
        try {
            RScript script = redissonClient.getScript();
            List<Object> keys = List.of(
                    COUPON_CACHE_PREFIX + couponId + ":quantity",
                    "SimpleFlashSale#couponUsers:" + couponId
            );
            List<Object> args = List.of(userId);

            String luaScript = loadLuaScript("LuaScript/restore_stock.lua");
            Long result = script.eval(RScript.Mode.READ_WRITE, luaScript, RScript.ReturnType.INTEGER, keys, args);

            logger.info("🔄 Redis Lua execution result: {}", result);
            if (result == 1) {
                logger.info("✅ Stock restored for coupon {} and user {} removed from purchase set.", couponId, userId);
            } else {
                logger.warn("⚠️ Redis Lua script execution returned unexpected result: {}", result);
            }
        } catch (Exception e) {
            logger.error("❌ Error executing Redis Lua script: {}", e.getMessage());
        }
    }

    private void publishToRedis(String serverId, String orderId, String status) {
        String message = String.format("{\"serverId\":\"%s\", \"orderId\":\"%s\", \"status\":\"%s\"}",
                serverId, orderId, status);
        String channel = "websocket-updates-" + serverId;
        redisTemplate.convertAndSend(channel, message);
        logger.info("📢 Published message to Redis channel: {}, status: {}", channel, status);
    }

    public String loadLuaScript(String scriptPath) {
        try (InputStream inputStream = getClass().getClassLoader().getResourceAsStream(scriptPath)) {
            if (inputStream == null) throw new RuntimeException("Lua script not found: " + scriptPath);
            return new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
                    .lines().collect(Collectors.joining("\n"));
        } catch (IOException e) {
            throw new RuntimeException("Failed to load Lua script: " + scriptPath, e);
        }
    }
}
