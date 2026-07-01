package ajou.aim_be.comment.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class CommentResponse {

    private Long commentId;
    private Long parentCommentId;

    private Long userId;
    private String authorName;
    private String department;
    private String profileImageUrl;

    private String content;
    private LocalDateTime createdAt;

    private boolean isDeleted;
    private boolean mine;

    private Visibility visibility;

    private List<CommentResponse> children;
}