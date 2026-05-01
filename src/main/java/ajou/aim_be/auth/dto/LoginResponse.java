package ajou.aim_be.auth.dto;

import ajou.aim_be.user.AdminRole;
import ajou.aim_be.user.UserRole;
import ajou.aim_be.user.UserStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class LoginResponse {

    @Schema(description = "유저 ID")
    private Long userId;

    @Schema(description = "유저 역할")
    private UserRole role;

    @Schema(description = "유저 상태 (ACTIVE, PENDING 등)")
    private UserStatus status;

    @Schema(description = "관리자 권한")
    private AdminRole adminRole;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "소속")
    private String department;

}