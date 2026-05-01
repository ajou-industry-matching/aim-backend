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
@Schema(description = "게시글 목록 응답 DTO")
public class PostListResponse {

    @Schema(description = "게시글 ID", example = "1")
    private Long postId;

    @Schema(description = "작성자 ID", example = "10")
    private Long userId;

    @Schema(description = "게시판 타입")
    private BoardType boardType;

    @Schema(description = "게시글 제목")
    private String title;

    @Schema(description = "게시글 설명")
    private String description;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "조회수", example = "123")
    private Long viewCount;

    @Schema(description = "좋아요 수", example = "15")
    private Long likeCount;

    @Schema(description = "댓글 수", example = "4")
    private Long commentCount;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "썸네일 이미지 URL")
    private String thumbnailImage;

    @Schema(description = "현재 로그인 사용자의 좋아요 여부", example = "true", nullable = true)
    private Boolean liked;

    @Schema(description = "키워드 목록")
    private List<KeywordResponse> keywords;

    public static PostListResponse of(
            Post post,
            List<KeywordResponse> keywords
    ) {
        return PostListResponse.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getUserId())
                .boardType(post.getBoardType())
                .title(post.getTitle())
                .description(post.getDescription())
                .visibility(post.getVisibility())
                .viewCount(post.getViewCount())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .thumbnailImage(post.getThumbnailImage())
                .keywords(keywords)
                .build();
    }
}