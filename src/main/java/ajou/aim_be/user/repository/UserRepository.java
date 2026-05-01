package ajou.aim_be.user.repository;

import ajou.aim_be.user.AdminRole;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserRole;
import ajou.aim_be.user.UserStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByFirebaseUid(String firebaseUid);

    @Query("""
        SELECT u FROM User u
        WHERE (:keyword IS NULL OR
               u.name LIKE %:keyword% OR
               u.email LIKE %:keyword%)
          AND (:role IS NULL OR u.userRole = :role)
          AND (:adminRole IS NULL OR u.adminRole = :adminRole)
          AND (:status IS NULL OR u.userStatus = :status)
    """)
    Page<User> searchUsers(
            @Param("keyword") String keyword,
            @Param("role") UserRole role,
            @Param("adminRole") AdminRole adminRole,
            @Param("status") UserStatus status,
            Pageable pageable
    );

}