package ajou.aim_be.crawling;

import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(
        name = "CRAWLED_PROJECT",
        uniqueConstraints = {
                @UniqueConstraint(
                        name = "uk_crawled_project_source",
                        columnNames = {"source_uid", "term"}
                )
        }
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrawledProject {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "crawled_project_id")
    private Long crawledProjectId;

    @Column(name = "source_uid", nullable = false, length = 100)
    private String sourceUid;

    @Column(length = 50)
    private String term;

    @Column(nullable = false)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String summary;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column(name = "source_url", length = 500)
    private String sourceUrl;

    @Column(name = "presentation_url", length = 500)
    private String presentationUrl;

    @Column(name = "video_url", length = 500)
    private String videoUrl;

    @Column(name = "github_url", length = 500)
    private String githubUrl;

    @Column(name = "representative_image", length = 500)
    private String representativeImage;

    @Column(length = 50)
    private String category;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(
            mappedBy = "project",
            cascade = CascadeType.ALL,
            orphanRemoval = true
    )
    @Builder.Default
    private List<CrawledProjectMember> members = new ArrayList<>();

    @PrePersist
    protected void onCreate() {
        createdAt = LocalDateTime.now();
        updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        updatedAt = LocalDateTime.now();
    }

    public void update(
            String title,
            String summary,
            String description,
            String content,
            String sourceUrl,
            String presentationUrl,
            String videoUrl,
            String githubUrl,
            String representativeImage,
            String category
    ) {
        this.title = title;
        this.summary = summary;
        this.description = description;
        this.content = content;
        this.sourceUrl = sourceUrl;
        this.presentationUrl = presentationUrl;
        this.videoUrl = videoUrl;
        this.githubUrl = githubUrl;
        this.representativeImage = representativeImage;
        this.category = category;
    }

    public void addMember(CrawledProjectMember member) {
        this.members.add(member);
    }

    public void clearMembers() {
        this.members.clear();
    }
}