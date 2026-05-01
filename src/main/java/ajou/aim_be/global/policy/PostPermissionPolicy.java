package ajou.aim_be.global.policy;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserRole;

public class PostPermissionPolicy {

    public static void validateCreatePermission(
            BoardType boardType,
            User user
    ) {
        UserActionPolicy.validateActive(user);

        UserRole role = user.getUserRole();

        switch (boardType) {

            case PORTFOLIO -> {
                if (role != UserRole.STUDENT) {
                    throw new CustomException(ErrorCode.PORTFOLIO_WRITE_FORBIDDEN);
                }
            }

            case LAB_INTERN -> {
                if (role != UserRole.PROFESSOR) {
                    throw new CustomException(ErrorCode.LAB_INTERN_WRITE_FORBIDDEN);
                }
            }

            case COMPANY_PROJECT -> {
                if (role != UserRole.COMPANY) {
                    throw new CustomException(ErrorCode.COMPANY_PROJECT_WRITE_FORBIDDEN);
                }
            }

            case NOTICE -> {
                if (!user.isAdmin()) {
                    throw new CustomException(ErrorCode.NOTICE_WRITE_FORBIDDEN);
                }
            }

            case CRAWLED_PROJECT -> {
                if (!user.isAdmin()) {
                    throw new CustomException(ErrorCode.CRAWLED_PROJECT_WRITE_FORBIDDEN);
                }
            }

            default -> throw new CustomException(ErrorCode.INVALID_BOARD_TYPE);
        }
    }
}