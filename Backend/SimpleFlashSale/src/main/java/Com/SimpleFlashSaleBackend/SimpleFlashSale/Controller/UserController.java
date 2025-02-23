package Com.SimpleFlashSaleBackend.SimpleFlashSale.Controller;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.CouponDTO;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.UserDTO;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Response.Response;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    // ✅ User Registration (No admin token needed)
    @PostMapping("/register")
    public ResponseEntity<Response<UserDTO>> registerUser(@RequestBody @Valid UserDTO userDTO) {
        Response<UserDTO> response = userService.registerUser(userDTO);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // ✅ User Login (Retrieve Token)
    @PostMapping("/login")
    public ResponseEntity<Response<String>> login(@RequestBody UserDTO userDTO) {
        Response<String> response = userService.login(userDTO.getUsername(), userDTO.getPassword());
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    @PostMapping("/logout")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<String>> logout(@RequestHeader("Authorization") String token) {
        Response<String> response = userService.logout(token);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // ✅ Get user profile by ID (Protected)
    @GetMapping("/{userId}")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<Response<UserDTO>> getUserProfile(@PathVariable String userId) {
        Response<UserDTO> response = userService.getUserProfile(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }

    // ✅ Get user coupons (Protected)
    @GetMapping("/{userId}/coupons")
    @PreAuthorize("isAuthenticated()") // Requires authentication
    public ResponseEntity<Response<List<CouponDTO>>> getUserCoupons(@PathVariable String userId) {
        Response<List<CouponDTO>> response = userService.getUserCoupons(userId);
        return ResponseEntity.status(response.getStatusCode()).body(response);
    }
}

