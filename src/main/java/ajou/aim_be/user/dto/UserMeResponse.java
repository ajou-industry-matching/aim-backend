package ajou.aim_be.user.dto;

import ajou.aim_be.user.User;
import ajou.aim_be.user.UserRole;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UserMeResponse {

    @Schema(description = "이름")
    private String name;

    @Schema(description = "작성한 게시글 수")
    private long postCount;

    @Schema(description = "작성한 게시글들이 받은 좋아요 수")
    private long likeCount;

    @Schema(description = "이메일")
    private String email;

    @Schema(description = "유저 역할")
    private UserRole role;

    @Schema(description = "학과 / 소속")
    private String department;

    @Schema(description = "프로필 설명")
    private String profileBio;

    private Long userId;

    public static UserMeResponse from(
            User user,
            long postCount,
            long likeCount
    ) {
        return UserMeResponse.builder()
                .name(user.getName())
                .postCount(postCount)
                .likeCount(likeCount)
                .email(user.getEmail())
                .role(user.getUserRole())
                .department(user.getDepartment())
                .profileBio(user.getProfileBio())
                .userId(user.getUserId())
                .build();
    }
}