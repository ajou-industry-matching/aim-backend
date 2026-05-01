package ajou.aim_be.auth.dto;

import ajou.aim_be.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    @Schema(description = "사용자 역할 (STUDENT, PROFESSOR 등)")
    private UserRole role;

    @Schema(description = "이름")
    private String name;

    @Schema(description = "소속 (학과/회사)")
    private String department;

}