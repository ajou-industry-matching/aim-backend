package ajou.aim_be.comment.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;

@Getter
public class CommentUpdateRequest {
    @Schema(description = "수정할 내용")
    private String content;

    @Schema(description = "공개 범위")
    private Visibility visibility;
}