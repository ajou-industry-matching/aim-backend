package ajou.aim_be.notification;

import ajou.aim_be.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "NOTIFICATIONS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Notification {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "notification_id")
    private Long notificationId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "notification_type", nullable = false)
    private NotificationType notificationType;

    @Column(name = "reference_id")
    private Long referenceId;

    @Column(name = "reference_type")
    private String referenceType;

    @Column(nullable = false)
    private String message;

    @Column(name = "is_read", nullable = false)
    @Builder.Default
    private Boolean isRead = false;

    @Column(name = "email_sent", nullable = false)
    @Builder.Default
    private Boolean emailSent = false;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "read_at")
    private LocalDateTime readAt;

    public void markAsRead() {
        this.isRead = true;
        this.readAt = LocalDateTime.now();
    }

    public void markAsUnread() {
        this.isRead = false;
        this.readAt = null;
    }

    public void markEmailAsSent() {
        this.emailSent = true;
    }

    public boolean belongsTo(Long userId) {
        return this.user.getUserId().equals(userId);
    }

    public boolean isUnread() {
        return !this.isRead;
    }
}