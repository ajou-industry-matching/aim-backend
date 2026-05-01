package ajou.aim_be.keyword;

import ajou.aim_be.post.Post;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import java.time.LocalDateTime;

@Entity
@Table(name = "POST_KEYWORDS",
        uniqueConstraints = @UniqueConstraint(columnNames = {"post_id", "keyword_id"}))
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class PostKeyword {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "post_keyword_id")
    private Long postKeywordId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "keyword_id", nullable = false)
    private Keyword keyword;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    public boolean belongsTo(Long postId) {
        return this.post.getPostId().equals(postId);
    }

    public boolean hasKeyword(Long keywordId) {
        return this.keyword.getKeywordId().equals(keywordId);
    }
}