package ajou.aim_be.global.policy;

import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserStatus;

public final class UserActionPolicy {

    private UserActionPolicy() {
    }

    public static void validateAuthenticated(User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }
    }

    public static void validateActive(User user) {
        validateAuthenticated(user);

        if (user.getUserStatus() != UserStatus.ACTIVE) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }
    }
}