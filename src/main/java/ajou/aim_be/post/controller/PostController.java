package ajou.aim_be.post.controller;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.post.dto.*;
import ajou.aim_be.post.service.PostCommandService;
import ajou.aim_be.post.service.PostQueryService;
import ajou.aim_be.user.User;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

import org.springframework.data.domain.Pageable;

import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;

import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
@Tag(name = "Post", description = "게시글 API")
public class PostController {

    private final PostQueryService postQueryService;
    private final PostCommandService postCommandService;

    @Operation(summary = "내 게시글 목록 조회", description = "현재 로그인한 사용자의 게시글 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/my")
    public PageResponse<PostListResponse> getMyPosts(
            @AuthenticationPrincipal User user,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        return postQueryService.getMyPosts(user, pageable);
    }

    @Operation(summary = "게시판별 게시글 목록 조회", description = "게시판 타입별 게시글 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/{boardType}")
    public PageResponse<PostListResponse> getPosts(
            @Parameter(description = "게시판 타입", example = "PORTFOLIO")
            @PathVariable BoardType boardType,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable,
            @AuthenticationPrincipal User user
    ) {

        return postQueryService.getPosts(boardType, pageable, user);
    }

    @Operation(summary = "게시글 상세 조회", description = "특정 게시글의 상세 내용을 조회합니다.")
    @GetMapping("/{boardType}/{postId}")
    public PostDetailResponse getPost(
            @Parameter(description = "게시판 타입", example = "PORTFOLIO")
            @PathVariable BoardType boardType,
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {

        return postQueryService.getPost(boardType, postId, user);
    }

    @Operation(summary = "게시글 작성", description = "게시글과 썸네일, 이미지, 일반 파일을 함께 업로드합니다.")
    @PreAuthorize("hasAnyRole('STUDENT','PROFESSOR','COMPANY','ADMIN')")
    @PostMapping(value = "/{boardType}", consumes = "multipart/form-data")
    public PostDetailResponse createPost(
            @Parameter(description = "게시판 타입", example = "PORTFOLIO")
            @PathVariable BoardType boardType,
            @AuthenticationPrincipal User user,
            @Parameter(description = "게시글 생성 요청 JSON")
            @RequestPart("request") PostCreateRequest request,
            @Parameter(description = "썸네일 이미지 파일")
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "본문 이미지 파일 목록")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(description = "일반 첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files
    ) {
        return postCommandService.createPost(boardType, user, request, thumbnail, images, files);
    }

    @Operation(summary = "게시글 수정", description = "게시글 정보와 첨부파일을 수정합니다.")
    @PreAuthorize("hasAnyRole('STUDENT','PROFESSOR','COMPANY','ADMIN')")
    @PutMapping(value = "/{boardType}/{postId}", consumes = "multipart/form-data")
    public PostDetailResponse updatePost(
            @Parameter(description = "게시판 타입", example = "PORTFOLIO")
            @PathVariable BoardType boardType,
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @Parameter(description = "게시글 수정 요청 JSON")
            @RequestPart("request") PostUpdateRequest request,
            @Parameter(description = "썸네일 이미지 파일")
            @RequestPart(value = "thumbnail", required = false) MultipartFile thumbnail,
            @Parameter(description = "추가 이미지 파일 목록")
            @RequestPart(value = "images", required = false) List<MultipartFile> images,
            @Parameter(description = "추가 일반 첨부파일 목록")
            @RequestPart(value = "files", required = false) List<MultipartFile> files,
            @AuthenticationPrincipal User user
    ) {
        return postCommandService.updatePost(boardType, postId, user, request, thumbnail, images, files);
    }

    @Operation(summary = "게시글 삭제", description = "게시글을 삭제합니다.")
    @PreAuthorize("hasAnyRole('STUDENT','PROFESSOR','COMPANY','ADMIN')")
    @DeleteMapping("/{boardType}/{postId}")
    public DeletePostResponse deletePost(
            @Parameter(description = "게시판 타입", example = "PORTFOLIO")
            @PathVariable BoardType boardType,
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {

        return postCommandService.deletePost(boardType, postId, user);
    }

    @Operation(summary = "좋아요한 게시글 조회", description = "현재 로그인한 사용자가 좋아요한 게시글 목록을 조회합니다.")
    @GetMapping("/liked")
    public PageResponse<PostListResponse> getLikedPosts(
            @AuthenticationPrincipal User user,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postQueryService.getLikedPosts(user, pageable);
    }

    @Operation(summary = "특정 사용자 게시글 조회", description = "특정 사용자가 작성한 게시글 목록을 조회합니다.")
    @GetMapping("/user/{userId}")
    public PageResponse<PostListResponse> getUserPosts(
            @Parameter(description = "사용자 ID", example = "10")
            @PathVariable Long userId,
            @AuthenticationPrincipal User user,
            Pageable pageable
    ) {
        return postQueryService.getUserPosts(userId, user, pageable);
    }

    @Operation(summary = "게시글 검색", description = "게시판 타입과 검색어로 게시글을 검색합니다.")
    @GetMapping("/search")
    public PageResponse<PostListResponse> searchPosts(
            @Parameter(description = "게시판 타입", example = "PORTFOLIO")
            @RequestParam BoardType boardType,
            @Parameter(description = "검색 키워드", example = "백엔드")
            @RequestParam(required = false) String keyword,
            @AuthenticationPrincipal User user,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
    ) {
        return postQueryService.searchPosts(boardType, keyword, user, pageable);
    }

    @Operation(summary = "최근 게시글 조회", description = "최근 게시글 4개를 조회합니다.")
    @GetMapping("/recent")
    public List<PostListResponse> getRecentPosts(
            @AuthenticationPrincipal User user
    ) {
        return postQueryService.getRecentPosts(user);
    }

    @Operation(summary = "최근 공지사항 조회", description = "최근 공지사항 게시글 4개를 조회합니다.")
    @GetMapping("/notices")
    public List<PostListResponse> getNoticePosts() {
        return postQueryService.getNoticesPosts();
    }
}