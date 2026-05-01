package ajou.aim_be.user;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "USERS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "user_id")
    private Long userId;

    @Column(unique = true, nullable = false)
    private String email;

    @Column(nullable = false)
    private String name;

    private String provider;

    private String department;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_role", nullable = false)
    private UserRole userRole;

    @Enumerated(EnumType.STRING)
    @Column(name = "user_status", nullable = false)
    private UserStatus userStatus;

    @Column(name = "profile_bio", columnDefinition = "TEXT")
    private String profileBio;

    @Column(name = "profile_image_url")
    private String profileImageUrl;

    private AdminRole adminRole;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "last_login_at")
    private LocalDateTime lastLoginAt;

    @Column(name = "firebase_uid", unique = true, nullable = false)
    private String firebaseUid;

    public void updateProfile(String profileBio, String profileImageUrl) {
        this.profileBio = profileBio;
        this.profileImageUrl = profileImageUrl;
    }

    public void updateLastLogin() {
        this.lastLoginAt = LocalDateTime.now();
    }

    public void changeRole(UserRole newRole) {
        this.userRole = newRole;
    }

    public void setAdminRole(AdminRole adminRole) {
        this.adminRole = adminRole;
    }

    public void updateDepartment(String department) {
        this.department = department;
    }

    public void changeStatus(UserStatus newStatus) {
        this.userStatus = newStatus;
    }

    public void block() {
        this.userStatus = UserStatus.BLOCKED;
    }

    public void activate() {
        this.userStatus = UserStatus.ACTIVE;
    }

    public boolean isAdmin(){
        return this.getAdminRole() == AdminRole.ADMIN || this.adminRole == AdminRole.SUPER_ADMIN;
    }

    // todo : 사용하는 걸로 바꾸기
    public boolean isActive() {
        return this.userStatus == UserStatus.ACTIVE;
    }

    public boolean isPending() {
        return this.userStatus == UserStatus.PENDING;
    }

    public boolean isCompany() {
        return this.userRole == UserRole.COMPANY;
    }

}