package ajou.aim_be.comment.controller;

import ajou.aim_be.comment.dto.CommentCommandResponse;
import ajou.aim_be.comment.dto.CommentCreateRequest;
import ajou.aim_be.comment.dto.CommentResponse;
import ajou.aim_be.comment.dto.CommentUpdateRequest;
import ajou.aim_be.comment.service.CommentCommandService;
import ajou.aim_be.comment.service.CommentQueryService;
import ajou.aim_be.post.dto.PageResponse;
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

@Tag(name = "Comment", description = "댓글 API")
@RestController
@RequestMapping("/api/comments")
@RequiredArgsConstructor
@PreAuthorize("isAuthenticated()")
public class CommentController {

    private final CommentCommandService commandService;
    private final CommentQueryService queryService;

    @Operation(summary = "댓글 작성", description = "게시글에 댓글 또는 대댓글을 작성합니다.")
    @PostMapping
    public CommentCommandResponse create(
            @AuthenticationPrincipal User user,
            @RequestBody CommentCreateRequest request
    ) {
        return commandService.createComment(user, request);
    }

    @Operation(summary = "댓글 조회", description = "게시글의 댓글 목록을 페이지 단위로 조회합니다.")
    @GetMapping("/{postId}")
    public PageResponse<CommentResponse> getComments(
            @Parameter(description = "게시글 ID", example = "100")
            @PathVariable Long postId,
            @AuthenticationPrincipal User user,
            @PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.ASC)
            Pageable pageable
    ) {
        return queryService.getComments(postId, user, pageable);
    }

    @Operation(summary = "댓글 수정", description = "본인이 작성한 댓글을 수정합니다.")
    @PutMapping("/{commentId}")
    public void update(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user,
            @RequestBody CommentUpdateRequest request
    ) {
        commandService.updateComment(commentId, user, request);
    }

    @Operation(summary = "댓글 삭제", description = "댓글을 삭제합니다.")
    @DeleteMapping("/{commentId}")
    public void delete(
            @Parameter(description = "댓글 ID", example = "100")
            @PathVariable Long commentId,
            @AuthenticationPrincipal User user
    ) {
        commandService.deleteComment(commentId, user);
    }
}
