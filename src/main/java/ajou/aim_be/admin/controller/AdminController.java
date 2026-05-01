package ajou.aim_be.admin.controller;

import ajou.aim_be.admin.dto.UserResponse;
import ajou.aim_be.admin.service.AdminService;
import ajou.aim_be.keyword.dto.DeleteKeywordResponse;
import ajou.aim_be.keyword.dto.KeywordCreateRequest;
import ajou.aim_be.keyword.service.KeywordService;
import ajou.aim_be.post.dto.KeywordResponse;
import ajou.aim_be.post.dto.PageResponse;
import ajou.aim_be.user.AdminRole;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserRole;
import ajou.aim_be.user.UserStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Admin", description = "관리자 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/admin")
@PreAuthorize("hasRole('ADMIN')")
public class AdminController {

    private final AdminService adminService;
    private final KeywordService keywordService;

    @Operation(summary = "기업 회원 승인")
    @PatchMapping("/users/{userId}/approve-company")
    public UserResponse approveCompany(@PathVariable Long userId) {

        return adminService.approveCompany(userId);
    }

    @Operation(summary = "유저 목록 조회")
    @GetMapping("/users")
    public PageResponse<UserResponse> getUsers(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) UserRole role,
            @RequestParam(required = false) AdminRole adminRole,
            @RequestParam(required = false) UserStatus status,
            @PageableDefault(size = 10) Pageable pageable
    ) {
        return adminService.getUsers(
                keyword,
                role,
                adminRole,
                status,
                pageable
        );
    }

    @Operation(summary = "관리자 권한 부여")
    @PatchMapping("/users/{userId}/grant-admin")
    public UserResponse grantAdmin(@PathVariable Long userId) {

        return adminService.grantAdmin(userId);
    }

    @Operation(summary = "관리자 권한 회수")
    @PatchMapping("/users/{userId}/revoke-admin")
    public UserResponse revokeAdmin(@PathVariable Long userId) {

        return adminService.revokeAdmin(userId);
    }

    @Operation(summary = "슈퍼 관리자 권한 부여")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/users/{userId}/grant-super-admin")
    public UserResponse grantSuperAdmin(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        return adminService.grantSuperAdmin(userId, user);
    }

    @Operation(summary = "슈퍼 관리자 권한 회수")
    @PreAuthorize("hasRole('SUPER_ADMIN')")
    @PatchMapping("/users/{userId}/revoke-super-admin")
    public UserResponse revokeSuperAdmin(@PathVariable Long userId, @AuthenticationPrincipal User user) {
        return adminService.revokeSuperAdmin(userId, user);
    }

    @Operation(summary = "키워드 생성")
    @PostMapping("/keywords")
    public KeywordResponse createKeyword(@RequestBody KeywordCreateRequest request) {
        return keywordService.createKeyword(request);
    }

    @Operation(summary = "키워드 생성")
    @PatchMapping("/keywords/{keywordId}")
    public KeywordResponse updateKeyword(
            @PathVariable Long keywordId,
            @RequestBody KeywordCreateRequest request
    ) {
        return keywordService.updateKeyword(keywordId, request.getKeywordName());
    }

    @Operation(summary = "키워드 삭제")
    @DeleteMapping("/keywords/{keywordId}")
    public DeleteKeywordResponse deleteKeyword(@PathVariable Long keywordId) {
        return keywordService.deleteKeyword(keywordId);
    }
}