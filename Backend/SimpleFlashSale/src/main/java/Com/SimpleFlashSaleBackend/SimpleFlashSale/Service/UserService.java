package Com.SimpleFlashSaleBackend.SimpleFlashSale.Service;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.CouponDTO;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.UserDTO;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.User;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.UserCoupon;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Mapper.UserMapper;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository.UserCouponRepository;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository.UserRepository;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Response.Response;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;
    private final KeycloakService keycloakService;

    private static final Logger log = LoggerFactory.getLogger(UserService.class);

    public UserService(UserRepository userRepository, KeycloakService keycloakService) {
        this.userRepository = userRepository;
        this.keycloakService = keycloakService;
    }

    // ✅ Register user
    @Transactional
    public Response<UserDTO> registerUser(UserDTO userDTO) {
        try {
            // ✅ Register User in Keycloak and get the Keycloak ID
            String keycloakUserId = keycloakService.registerUser(userDTO);

            // ✅ Save user in database using Keycloak ID
            userDTO.setCredit(100f);
            User user = UserMapper.toEntity(userDTO);
            user.setId(keycloakUserId);
            User savedUser = userRepository.save(user);

            return new Response<>(200, "User registered successfully!", UserMapper.toDTO(savedUser));
        } catch (Exception e) {
            return new Response<>(400, "User registration failed: " + e.getMessage(), null);
        }
    }

    @Transactional
    public Response<UserDTO> getUserProfile(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        return new Response<>(200, "User profile retrieved successfully!", UserMapper.toDTO(user));
    }

    // ✅ User Login (Retrieve Token)
    public Response<String> login(String username, String password) {
        try {
            String token = keycloakService.getUserToken(username, password);
            return new Response<>(200, "Login successful!", "Bearer " + token);
        } catch (Exception e) {
            return new Response<>(401, "Invalid credentials!", null);
        }
    }

    // ✅ User Logout (Invalidate Keycloak Session)
    public Response<String> logout(String token) {
        try {
            boolean isLoggedOut = keycloakService.logoutUser(token);
            if (isLoggedOut) {
                return new Response<>(200, "Logout successful!", null);
            }
            return new Response<>(400, "Logout failed!", null);
        } catch (Exception e) {
            return new Response<>(500, "Error logging out: " + e.getMessage(), null);
        }
    }

    @Transactional
    public Response<List<CouponDTO>> getUserCoupons(String userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new RuntimeException("User not found"));

        List<CouponDTO> coupons = user.getUserCoupons().stream()
                .map(userCoupon -> new CouponDTO(
                        userCoupon.getCoupon().getId(),
                        userCoupon.getCoupon().getName(),
                        userCoupon.getCoupon().getImageURL(),
                        userCoupon.getCoupon().getPrice(),
                        userCoupon.getCoupon().getDescription(),
                        userCoupon.getCoupon().getQuantity(),
                        userCoupon.isPaymentSuccess(),
                        userCoupon.getCoupon().isDeleted()
                ))
                .collect(Collectors.toList());

        return new Response<>(200, "User coupons retrieved successfully!", coupons);
    }
}
