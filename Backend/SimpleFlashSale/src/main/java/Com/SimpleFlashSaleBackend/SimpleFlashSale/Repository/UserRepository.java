package Com.SimpleFlashSaleBackend.SimpleFlashSale.Repository;

import Com.SimpleFlashSaleBackend.SimpleFlashSale.Entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface UserRepository extends JpaRepository<User, String> {
    // Find user by email
    Optional<User> findByEmail(String email);

    // Find user by username
    Optional<User> findByUsername(String username);
}
