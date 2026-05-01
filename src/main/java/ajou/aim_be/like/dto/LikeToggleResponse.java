package ajou.aim_be.like.dto;


import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "좋아요 토글 응답 DTO")
public class LikeToggleResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "좋아요 여부", example = "true")
    private boolean liked;

    @Schema(description = "변경 후 좋아요 수", example = "11")
    private Long likeCount;
}