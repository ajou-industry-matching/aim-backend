package ajou.aim_be.admin.dto;

import ajou.aim_be.user.AdminRole;
import ajou.aim_be.user.User;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Builder
@Getter
@Schema(description = "관리자용 유저 응답 DTO")
public class UserResponse {

    @Schema(description = "유저 ID")
    private Long userId;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "소속")
    private String department;

    @Schema(description = "역할")
    private String role;

    @Schema(description = "관리자 권한")
    private AdminRole adminRole;

    @Schema(description = "상태")
    private String status;

    @Schema(description = "생성일")
    private LocalDateTime createdAt;

    public static UserResponse from(User user) {
        return UserResponse.builder()
        .userId(user.getUserId())
        .email(user.getEmail())
        .name(user.getName())
        .department(user.getDepartment())
        .role(user.getUserRole().name())
        .adminRole(user.getAdminRole())
        .status(user.getUserStatus().name())
        .createdAt(user.getCreatedAt())
        .build();
    }
}