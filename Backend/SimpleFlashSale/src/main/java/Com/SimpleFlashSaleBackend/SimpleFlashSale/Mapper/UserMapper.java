package Com.SimpleFlashSaleBackend.SimpleFlashSale.Mapper;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto.UserDTO;
import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.User;
import org.springframework.stereotype.Component;

import java.util.Set;
import java.util.stream.Collectors;

public class UserMapper {

    // Convert Entity to DTO
    public static UserDTO toDTO(User user) {
        if (user == null) {
            return null;
        }
        return new UserDTO(
                user.getId(),
                user.getUsername(),
                user.getEmail(),
                null, // Password should not be exposed in DTO
                user.getCredit(),
                user.getUserCoupons().stream()
                        .map(userCoupon -> userCoupon.getCoupon().getId())
                        .collect(Collectors.toSet())
        );
    }

    // Convert DTO to Entity
    public static User toEntity(UserDTO dto) {
        if (dto == null) {
            return null;
        }
        User user = new User();
        user.setId(dto.getId());
        user.setUsername(dto.getUsername());
        user.setEmail(dto.getEmail());
        user.setCredit(dto.getCredit());
        return user; // Coupon relationships will be handled separately
    }
}

