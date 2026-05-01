package ajou.aim_be.post.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "게시글 삭제 응답 DTO")
public class DeletePostResponse {

    @Schema(description = "삭제된 게시글 ID", example = "100")
    private Long postId;
}