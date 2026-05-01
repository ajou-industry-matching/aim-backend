package ajou.aim_be.like.service;

import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.global.policy.UserActionPolicy;
import ajou.aim_be.like.Like;
import ajou.aim_be.like.dto.LikeToggleResponse;
import ajou.aim_be.like.repository.LikeRepository;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.user.User;
import ajou.aim_be.user.UserStatus;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class LikeCommandService {

    private final LikeRepository likeRepository;
    private final PostRepository postRepository;

    @Transactional
    public LikeToggleResponse toggleLike(Long postId, User user) {

        UserActionPolicy.validateActive(user);

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validatePostLikePermission(post, user);

        boolean liked = likeRepository.findByPost_PostIdAndUser_UserId(postId, user.getUserId())
                .map(existing -> {
                    likeRepository.delete(existing);
                    post.decreaseLikeCount();
                    return false;
                })
                .orElseGet(() -> {
                    try {
                        Like like = Like.builder()
                                .post(post)
                                .user(user)
                                .build();

                        likeRepository.save(like);
                        post.increaseLikeCount();
                        return true;

                    } catch (DataIntegrityViolationException e) {
                        return likeRepository.existsByPost_PostIdAndUser_UserId(postId, user.getUserId());                    }
                });

        return LikeToggleResponse.builder()
                .postId(postId)
                .liked(liked)
                .likeCount(post.getLikeCount())
                .build();
    }

    private void validatePostLikePermission(Post post, User user) {

        if (post.getVisibility() == Visibility.PRIVATE && !post.isOwner(user.getUserId())) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}