package ajou.aim_be.comment.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
@Schema(description = "댓글 생성 요청 DTO")
public class CommentCreateRequest {

    @Schema(description = "게시글 ID", requiredMode = Schema.RequiredMode.REQUIRED, example = "100")
    private Long postId;

    @Schema(description = "댓글 내용", requiredMode = Schema.RequiredMode.REQUIRED, example = "좋은 글 감사합니다.")
    private String content;

    @Schema(description = "부모 댓글 ID (대댓글인 경우)", nullable = true, example = "1")
    private Long parentCommentId;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private Visibility visibility;
}