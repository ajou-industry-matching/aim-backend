package ajou.aim_be.post;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.keyword.PostKeyword;
import ajou.aim_be.user.User;
import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "POSTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Post {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_id")
    private Long postId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(name = "board_type", nullable = false)
    private BoardType boardType;

    @Column(nullable = false)
    private String title;

    @Column(nullable = false)
    private String description;

    @Column(columnDefinition = "TEXT")
    private String content;

    @Column
    private String thumbnailImage;

    @Column
    private String thumbnailStorageKey;

    @Column
    private String videoLink;

    @Column
    private String githubLink;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @Column(name = "view_count", nullable = false)
    @Builder.Default
    private Long viewCount = 0L;

    @Column(name = "like_count", nullable = false)
    @Builder.Default
    private Long likeCount = 0L;

    @Column(name = "comment_count", nullable = false)
    @Builder.Default
    private Long commentCount = 0L;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "post", fetch = FetchType.LAZY)
    @JsonIgnore
    private List<PostKeyword> postKeywords = new ArrayList<>();

    public void update(String title, String description, String content, String githubLink, String videoLink) {
        this.title = title;
        this.description = description;
        this.content = content;
        this.githubLink = githubLink;
        this.videoLink = videoLink;
    }

    public void updateThumbnail(String url, String storageKey) {
        this.thumbnailImage = url;
        this.thumbnailStorageKey = storageKey;
    }
    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public void increaseViewCount() {
        this.viewCount++;
    }

    public void increaseLikeCount() {
        this.likeCount++;
    }

    public void decreaseLikeCount() {
        if (this.likeCount > 0) {
            this.likeCount--;
        }
    }

    public void increaseCommentCount() {
        this.commentCount++;
    }

    public void decreaseCommentCount() {
        if (this.commentCount > 0) {
            this.commentCount--;
        }
    }

    public boolean isOwner(Long userId) {
        return this.user.getUserId().equals(userId);
    }

    public boolean isPublic() {
        return this.visibility == Visibility.PUBLIC;
    }
}