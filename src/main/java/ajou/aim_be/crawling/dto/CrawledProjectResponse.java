package ajou.aim_be.crawling.dto;

import ajou.aim_be.crawling.CrawledProject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
@Schema(description = "크롤링 프로젝트 응답 DTO")
public class CrawledProjectResponse {

    @Schema(description = "크롤링 프로젝트 ID", example = "1")
    private Long crawledProjectId;

    @Schema(description = "원본 프로젝트 식별자", example = "2025-001")
    private String sourceUid;

    @Schema(description = "크롤링 기간 또는 학기", example = "2025-1")
    private String term;

    @Schema(description = "프로젝트 제목", example = "AI 기반 프로젝트")
    private String title;

    @Schema(description = "프로젝트 요약", example = "AI를 활용한 프로젝트입니다.")
    private String summary;

    @Schema(description = "프로젝트 설명")
    private String description;

    @Schema(description = "프로젝트 본문")
    private String content;

    @Schema(description = "원본 프로젝트 URL")
    private String sourceUrl;

    @Schema(description = "발표 자료 URL")
    private String presentationUrl;

    @Schema(description = "영상 URL")
    private String videoUrl;

    @Schema(description = "GitHub URL")
    private String githubUrl;

    @Schema(description = "대표 이미지 URL")
    private String representativeImage;

    @Schema(description = "프로젝트 카테고리", example = "WEB")
    private String category;

    @Schema(description = "프로젝트 참여자 목록")
    private List<CrawledProjectMemberResponse> members;

    @Schema(description = "프로젝트 생성일시")
    private LocalDateTime createdAt;

    public static CrawledProjectResponse from(
            CrawledProject project,
            List<CrawledProjectMemberResponse> members
    ) {
        return CrawledProjectResponse.builder()
                .crawledProjectId(project.getCrawledProjectId())
                .sourceUid(project.getSourceUid())
                .term(project.getTerm())
                .title(project.getTitle())
                .summary(project.getSummary())
                .description(project.getDescription())
                .content(project.getContent())
                .sourceUrl(project.getSourceUrl())
                .presentationUrl(project.getPresentationUrl())
                .videoUrl(project.getVideoUrl())
                .githubUrl(project.getGithubUrl())
                .representativeImage(project.getRepresentativeImage())
                .category(project.getCategory())
                .members(members)
                .createdAt(project.getCreatedAt())
                .build();
    }
}