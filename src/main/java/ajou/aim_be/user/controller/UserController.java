package ajou.aim_be.user.controller;

import ajou.aim_be.user.User;
import ajou.aim_be.user.dto.UpdateMyInfoRequest;
import ajou.aim_be.user.dto.UserMeResponse;
import ajou.aim_be.user.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/users")
@Tag(name = "User", description = "사용자 API")
public class UserController {

    private final UserService userService;

    @Operation(summary = "내 프로필 조회", description = "현재 로그인한 사용자의 프로필 정보를 조회합니다.")
    @GetMapping("/me")
    public ResponseEntity<UserMeResponse> getMyInfo(
            @AuthenticationPrincipal User currentUser
    ) {
        return ResponseEntity.ok(userService.getMyInfo(currentUser));
    }

    @Operation(summary = "내 프로필 수정", description = "현재 로그인한 사용자의 이름과 프로필 설명을 수정합니다.")
    @PatchMapping("/me")
    public ResponseEntity<UserMeResponse> updateMyInfo(
            @AuthenticationPrincipal User currentUser,
            @RequestBody UpdateMyInfoRequest request
    ) {
        return ResponseEntity.ok(userService.updateMyInfo(currentUser, request));
    }
}