package ajou.aim_be.post.dto;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.post.Post;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "게시글 상세 응답 DTO")
public class PostDetailResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "작성자 ID", example = "10")
    private Long userId;

    @Schema(description = "게시판 타입", example = "PORTFOLIO")
    private BoardType boardType;

    @Schema(description = "게시글 제목", example = "백엔드 포트폴리오")
    private String title;

    @Schema(description = "게시글 본문", example = "프로젝트 상세 설명")
    private String content;

    @Schema(description = "게시글 설명", example = "간단한 소개")
    private String description;

    @Schema(description = "첨부 영상 URL", example = "https://youtube.com/example")
    private String videoLink;

    @Schema(description = "깃허브 URL", example = "https://github.com/example/repo")
    private String githubLink;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "조회수", example = "100")
    private Long viewCount;

    @Schema(description = "좋아요 수", example = "20")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "3")
    private Long commentCount;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "썸네일 이미지 URL", example = "https://storage.example.com/thumb.png")
    private String thumbnailImage;

    @Schema(description = "현재 로그인 사용자의 좋아요 여부", example = "false", nullable = true)
    private Boolean liked;

    @Schema(description = "키워드 목록")
    private List<KeywordResponse> keywords;

    @Schema(description = "이미지 첨부 목록")
    private List<AttachmentResponse> images;

    @Schema(description = "일반 파일 첨부 목록")
    private List<AttachmentResponse> files;

    public static PostDetailResponse of(
            Post post,
            List<KeywordResponse> keywords,
            List<AttachmentResponse> images,
            List<AttachmentResponse> files
    ) {
        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getUserId())
                .boardType(post.getBoardType())
                .title(post.getTitle())
                .content(post.getContent())
                .description(post.getDescription())
                .githubLink(post.getGithubLink())
                .videoLink(post.getVideoLink())
                .visibility(post.getVisibility())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .thumbnailImage(post.getThumbnailImage())
                .keywords(keywords)
                .images(images)
                .files(files)
                .build();
    }
}