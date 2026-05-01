package ajou.aim_be.auth.service;

import ajou.aim_be.auth.dto.LoginRequest;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserRole;
import ajou.aim_be.user.UserStatus;
import ajou.aim_be.user.repository.UserRepository;
import com.google.firebase.auth.FirebaseToken;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;

    @Transactional
    public User loginOrRegister(FirebaseToken token, LoginRequest request) {

        String uid = token.getUid();

        Optional<User> optionalUser = userRepository.findByFirebaseUid(uid);

        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (request.getRole() != null && user.getUserRole() != request.getRole()) {
                throw new CustomException(ErrorCode.INVALID_ROLE);
            }

            user.updateLastLogin();
            return user;
        }

        String email = token.getEmail();
        if (email == null || email.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_TOKEN);
        }

        String name = request.getName();
        if (name == null || name.isBlank()) {
            name = token.getName();
        }
        if (name == null || name.isBlank()) {
            name = email.split("@")[0];
        }

        if (request.getRole() == null) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        String department = request.getDepartment();
        if (department == null || department.isBlank()) {
            throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
        }

        UserStatus status;
        if (request.getRole() == UserRole.COMPANY){
            status = UserStatus.PENDING;
        } else{
            status = UserStatus.ACTIVE;
        }

        User user = User.builder()
                .firebaseUid(uid)
                .email(email)
                .name(name)
                .department(department)
                .provider(token.getIssuer())
                .userRole(request.getRole())
                .userStatus(status)
                .build();

        user.updateLastLogin();

        return userRepository.save(user);
    }
}