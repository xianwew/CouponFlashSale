package Com.SimpleFlashSaleBackend.SimpleFlashSale.Dto;

import lombok.*;

import java.util.Set;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserDTO {
    private String id;
    private String username;
    private String email;
    private String password;
    private Float credit;
    private Set<Long> couponIds; // Store IDs of purchased coupons
}
