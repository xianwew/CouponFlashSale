package Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.UserCoupon;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserCouponRepository extends JpaRepository<UserCoupon, Long> {
    // Get all coupons a user has purchased
    List<UserCoupon> findByUserId(String userId);
}