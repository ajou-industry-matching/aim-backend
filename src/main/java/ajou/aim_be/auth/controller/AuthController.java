package ajou.aim_be.auth.controller;

import ajou.aim_be.auth.dto.LoginRequest;
import ajou.aim_be.auth.dto.LoginResponse;
import ajou.aim_be.auth.service.AuthService;
import ajou.aim_be.auth.service.FirebaseAuthService;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.user.User;
import com.google.firebase.auth.FirebaseToken;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Auth", description = "인증 API")
public class AuthController {

    private final FirebaseAuthService firebaseAuthService;
    private final AuthService authService;

    @Operation(summary = "로그인 / 회원가입", description = "Firebase 토큰 기반 로그인 및 자동 회원가입 처리")
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(
            @Parameter(description = "Bearer 토큰", example = "Bearer eyJhbGciOiJIUzI1NiIs...")
            @RequestHeader("Authorization") String authHeader,
            @Parameter(description = "유저 정보")
            @RequestBody LoginRequest request
    ) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            throw new CustomException(ErrorCode.INVALID_HEADER);
        }

        String token = authHeader.substring(7);

        FirebaseToken decoded = firebaseAuthService.verifyToken(token);

        User user = authService.loginOrRegister(decoded, request);

        LoginResponse response = LoginResponse.builder()
                .userId(user.getUserId())
                .role(user.getUserRole())
                .status(user.getUserStatus())
                .build();

        return ResponseEntity.ok(response);
    }
}