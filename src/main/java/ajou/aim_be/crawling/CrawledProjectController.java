package ajou.aim_be.crawling;

import ajou.aim_be.crawling.dto.CrawledProjectCreateRequest;
import ajou.aim_be.crawling.dto.CrawledProjectResponse;
import ajou.aim_be.post.dto.PostDetailResponse;
import ajou.aim_be.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/crawled-projects")
@RequiredArgsConstructor
@Tag(name = "Crawled Project", description = "크롤링 프로젝트 API")
public class CrawledProjectController {

    private final CrawledProjectCommandService commandService;
    private final CrawledProjectQueryService queryService;

    @Operation(
            summary = "크롤링 프로젝트 저장",
            description = "관리자가 크롤링한 프로젝트 데이터를 저장합니다."
    )
    @PreAuthorize("hasAnyRole('ADMIN', 'SUPER_ADMIN')")
    @PostMapping
    public ResponseEntity<Void> create(
            @RequestBody CrawledProjectCreateRequest request,
            @AuthenticationPrincipal User user
    ) {
        commandService.save(request, user);

        return ResponseEntity.ok().build();
    }

    @Operation(
            summary = "내가 참여한 크롤링 프로젝트 조회",
            description = "현재 로그인한 사용자가 참여한 프로젝트 목록을 조회합니다."
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'COMPANY', 'ADMIN')")
    @GetMapping("/my")
    public List<CrawledProjectResponse> getMyProjects(
            @AuthenticationPrincipal User user
    ) {
        return queryService.getMyProjects(user);
    }

    @Operation(
            summary = "크롤링 프로젝트 상세 조회",
            description = "크롤링 프로젝트의 상세 정보를 조회합니다."
    )
    @PreAuthorize("hasAnyRole('STUDENT', 'PROFESSOR', 'COMPANY', 'ADMIN')")
    @GetMapping("/{projectId}")
    public CrawledProjectResponse getProject(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user
    ) {
        return queryService.getProject(projectId, user);
    }

    @Operation(
            summary = "크롤링 프로젝트를 포트폴리오로 가져오기",
            description = "현재 로그인한 사용자가 참여한 크롤링 프로젝트를 자신의 포트폴리오 게시글로 복제합니다."
    )
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{projectId}/portfolio")
    public PostDetailResponse cloneToPortfolio(
            @PathVariable Long projectId,
            @AuthenticationPrincipal User user
    ) {
        return commandService.cloneToPortfolio(
                projectId,
                user
        );
    }
}