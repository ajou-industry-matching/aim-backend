package ajou.aim_be.admin.service;

import ajou.aim_be.admin.dto.UserResponse;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.post.dto.PageResponse;
import ajou.aim_be.user.AdminRole;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserRole;
import ajou.aim_be.user.UserStatus;
import ajou.aim_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private final UserRepository userRepository;

    public UserResponse approveCompany(Long userId) {

        User user = getUser(userId);

        if (user.getUserRole() != UserRole.COMPANY) {
            throw new CustomException(ErrorCode.INVALID_ROLE);
        }

        if (user.getUserStatus() != UserStatus.PENDING) {
            throw new CustomException(ErrorCode.INVALID_STATUS);
        }

        user.changeStatus(UserStatus.ACTIVE);

        return UserResponse.builder()
                .userId(user.getUserId())
                .role(user.getUserRole().name())
                .adminRole(user.getAdminRole())
                .status(user.getUserStatus().name())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<UserResponse> getUsers(
            String keyword,
            UserRole role,
            AdminRole adminRole,
            UserStatus status,
            Pageable pageable
    ) {

        Page<User> users = userRepository.searchUsers(
                keyword,
                role,
                adminRole,
                status,
                pageable
        );

        return PageResponse.from(
                users.map(UserResponse::from)
        );
    }

    @Transactional
    public UserResponse grantAdmin(Long userId) {
        User user = getUser(userId);

        user.setAdminRole(AdminRole.ADMIN);

        return UserResponse.builder()
                .userId(user.getUserId())
                .role(user.getUserRole().name())
                .adminRole(user.getAdminRole())
                .status(user.getUserStatus().name())
                .build();
    }

    @Transactional
    public UserResponse revokeAdmin(Long userId) {
        User user = getUser(userId);

        user.setAdminRole(AdminRole.NONE);

        return UserResponse.builder()
                .userId(user.getUserId())
                .role(user.getUserRole().name())
                .adminRole(user.getAdminRole())
                .status(user.getUserStatus().name())
                .build();
    }

    @Transactional
    public UserResponse grantSuperAdmin(Long userId, User requester) {

        if (requester.getAdminRole()!=AdminRole.SUPER_ADMIN) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        User user = getUser(userId);
        user.setAdminRole(AdminRole.SUPER_ADMIN);

        return UserResponse.builder()
                .userId(user.getUserId())
                .role(user.getUserRole().name())
                .adminRole(user.getAdminRole())
                .status(user.getUserStatus().name())
                .build();
    }

    @Transactional
    public UserResponse revokeSuperAdmin(Long userId, User requester) {

        if (requester.getAdminRole()!=AdminRole.SUPER_ADMIN) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        User user = getUser(userId);
        user.setAdminRole(AdminRole.NONE);

        return UserResponse.builder()
                .userId(user.getUserId())
                .role(user.getUserRole().name())
                .adminRole(user.getAdminRole())
                .status(user.getUserStatus().name())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ErrorCode.USER_NOT_FOUND));
    }
}