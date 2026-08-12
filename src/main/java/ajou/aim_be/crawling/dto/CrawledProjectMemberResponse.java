package ajou.aim_be.crawling.dto;

import ajou.aim_be.crawling.CrawledProjectMember;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "크롤링 프로젝트 참여자 응답 DTO")
public class CrawledProjectMemberResponse {

    @Schema(description = "참여자 역할", example = "BACKEND")
    private String role;

    @Schema(description = "이름", example = "홍길동")
    private String name;

    @Schema(description = "마스킹된 이메일", example = "abc**@ajou.ac.kr")
    private String maskedEmail;

    @Schema(description = "학과", example = "소프트웨어학과")
    private String department;

    @Schema(description = "학년", example = "4")
    private String grade;

    public static CrawledProjectMemberResponse from(
            CrawledProjectMember member
    ) {
        return CrawledProjectMemberResponse.builder()
                .role(member.getRole())
                .name(member.getName())
                .maskedEmail(member.getMaskedEmail())
                .department(member.getDepartment())
                .grade(member.getGrade())
                .build();
    }
}