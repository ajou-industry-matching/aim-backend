package ajou.aim_be.like.controller;

import ajou.aim_be.like.dto.LikeToggleResponse;
import ajou.aim_be.like.service.LikeCommandService;
import ajou.aim_be.user.User;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@Tag(name = "Like", description = "게시글 좋아요 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/posts")
public class LikeController {

    private final LikeCommandService likeCommandService;


    @Operation(summary = "좋아요 토글", description = "게시글 좋아요를 추가하거나 취소합니다.")
    @PostMapping("/{postId}/like")
    public LikeToggleResponse toggleLike(
            @Parameter(description = "게시글 ID", example = "1")
            @PathVariable Long postId,
            @AuthenticationPrincipal User user
    ) {
        return likeCommandService.toggleLike(postId, user);
    }
}