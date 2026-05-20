package ajou.aim_be.user.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateMyInfoRequest {

    @Schema(description = "이름")
    private String name;

    @Schema(description = "프로필 설명")
    private String profileBio;
}