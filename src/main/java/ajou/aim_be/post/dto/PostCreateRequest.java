package ajou.aim_be.post.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
public class PostCreateRequest {

    @Schema(description = "게시글 제목", requiredMode = Schema.RequiredMode.REQUIRED)
    private String title;

    @Schema(description = "게시글 설명")
    private String description;

    @Schema(description = "게시글 본문")
    private String content;

    @Schema(description = "첨부 영상 URL")
    private String videoLink;

    @Schema(description = "깃허브 URL")
    private String githubLink;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "선택한 키워드 ID 목록", example = "[1, 2, 3]")
    private List<Long> keywordIds = new ArrayList<>();
}