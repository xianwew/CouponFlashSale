package Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity;

import jakarta.persistence.*;
import lombok.*;

import java.util.HashSet;
import java.util.Set;

@Entity
@Table(name = "users")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class User {
    @Id
    private String id; // Store Keycloak UUID instead of auto-incremented ID

    @Column(nullable = false, unique = true)
    private String username;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private Float credit;

    // Define relationship with coupons (Many-to-Many)
    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<UserCoupon> userCoupons = new HashSet<>();
}

