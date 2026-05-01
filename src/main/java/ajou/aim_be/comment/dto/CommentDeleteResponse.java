package ajou.aim_be.comment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "댓글 삭제 응답 DTO")
public class CommentDeleteResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "100")
    private Long postId;

    @Schema(description = "소프트 삭제 여부 (true면 '삭제된 댓글입니다' 처리)", example = "true")
    private boolean softDeleted;

    @Schema(description = "변경 후 게시글 댓글 수", example = "3")
    private Long commentCount;
}
