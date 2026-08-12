package ajou.aim_be.crawling;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "CRAWLED_PROJECT_MEMBER",
        indexes = {
                @Index(
                        name = "idx_crawled_member_project",
                        columnList = "crawled_project_id"
                ),
                @Index(
                        name = "idx_crawled_member_match",
                        columnList = "name,email_prefix"
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrawledProjectMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawled_project_member_id")
    private Long crawledProjectMemberId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(
            name = "crawled_project_id",
            nullable = false
    )
    private CrawledProject project;

    @Column(length = 100)
    private String role;

    @Column(nullable = false, length = 100)
    private String name;

    @Column(name = "masked_email", length = 255)
    private String maskedEmail;

    @Column(name = "email_prefix", length = 100)
    private String emailPrefix;

    @Column(length = 100)
    private String department;

    @Column(length = 50)
    private String grade;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
    }
}