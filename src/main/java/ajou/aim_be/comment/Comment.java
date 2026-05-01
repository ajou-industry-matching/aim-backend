package ajou.aim_be.comment;

import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.post.Post;
import ajou.aim_be.user.User;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "COMMENTS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class Comment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "comment_id")
    private Long commentId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private Post post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_comment_id")
    private Comment parentComment;

    @OneToMany(mappedBy = "parentComment", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Comment> childComments = new ArrayList<>();

    @Column(columnDefinition = "TEXT", nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Visibility visibility = Visibility.PUBLIC;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private boolean isDeleted = false;

    public void updateContent(String content) {
        this.content = content;
    }

    public void changeVisibility(Visibility visibility) {
        this.visibility = visibility;
    }

    public boolean isOwner(Long userId) {
        return this.user.getUserId().equals(userId);
    }

    public boolean isReply() {
        return this.parentComment != null;
    }

    public void addChildComment(Comment child) {
        this.childComments.add(child);
    }

    public void softDelete() {
        this.isDeleted = true;
        this.content = "삭제된 댓글입니다";
    }
}