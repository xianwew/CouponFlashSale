package Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity;


import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "user_coupons")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserCoupon {
    @Id
    private String id; // ✅ Change to UUID

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "coupon_id", nullable = false)
    private Coupon coupon;

    @Column(nullable = false)
    private boolean paymentSuccess = false;

    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();
}