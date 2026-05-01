package ajou.aim_be.post.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Getter
@NoArgsConstructor
@Schema(description = "게시글 수정 요청 DTO")
public class PostUpdateRequest {

    @Schema(description = "게시글 제목", example = "수정된 게시글 제목")
    private String title;

    @Schema(description = "게시글 본문", example = "수정된 게시글 본문")
    private String content;

    @Schema(description = "게시글 설명", example = "수정된 게시글 설명")
    private String description;

    @Schema(description = "첨부 영상 URL", example = "https://www.youtube.com/watch?v=updated")
    private String videoLink;

    @Schema(description = "깃허브 URL", example = "https://github.com/example/updated-repository")
    private String githubLink;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "선택한 키워드 ID 목록", example = "[1, 2, 3]")
    private List<Long> keywordIds = new ArrayList<>();

    @Schema(description = "삭제할 첨부파일 ID 목록", example = "[10, 11]")
    private List<Long> deleteAttachmentIds = new ArrayList<>();
}