package ajou.aim_be.user.service;

import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.global.policy.UserActionPolicy;
import ajou.aim_be.like.repository.LikeRepository;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.user.User;
import ajou.aim_be.user.dto.UpdateMyInfoRequest;
import ajou.aim_be.user.dto.UserMeResponse;
import ajou.aim_be.user.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final PostRepository postRepository;
    private final LikeRepository likeRepository;

    @Transactional(readOnly = true)
    public UserMeResponse getMyInfo(User currentUser) {
        UserActionPolicy.validateAuthenticated(currentUser);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        long postCount = postRepository.countByUser_UserId(user.getUserId());
        long likeCount = likeRepository.countByPost_User_UserId(user.getUserId());

        return UserMeResponse.from(user, postCount, likeCount);
    }

    @Transactional
    public UserMeResponse updateMyInfo(
            User currentUser,
            UpdateMyInfoRequest request
    ) {
        UserActionPolicy.validateActive(currentUser);

        User user = userRepository.findById(currentUser.getUserId())
                .orElseThrow(() -> new CustomException(ErrorCode.UNAUTHORIZED));

        if (request.getName() != null) {
            if (request.getName().isBlank()) {
                throw new CustomException(ErrorCode.INVALID_INPUT_VALUE);
            }

            user.updateName(request.getName().trim());
        }

        if (request.getProfileBio() != null) {
            user.updateProfileBio(request.getProfileBio());
        }

        long postCount = postRepository.countByUser_UserId(user.getUserId());
        long likeCount = likeRepository.countByPost_User_UserId(user.getUserId());

        return UserMeResponse.from(user, postCount, likeCount);
    }
}