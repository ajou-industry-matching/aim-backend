package ajou.aim_be.comment.service;

import ajou.aim_be.comment.Comment;
import ajou.aim_be.comment.dto.CommentCommandResponse;
import ajou.aim_be.comment.dto.CommentCreateRequest;
import ajou.aim_be.comment.dto.CommentUpdateRequest;
import ajou.aim_be.comment.dto.CommentDeleteResponse;
import ajou.aim_be.comment.repository.CommentRepository;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.global.policy.UserActionPolicy;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CommentCommandService {

    private final PostRepository postRepository;
    private final CommentRepository commentRepository;

    @Transactional
    public CommentCommandResponse createComment(User user, CommentCreateRequest request) {

        UserActionPolicy.validateActive(user);

        Post post = postRepository.findById(request.getPostId())
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        Comment parent = null;

        validatePostCommentPermission(post, user);

        if (request.getParentCommentId() != null) {
            parent = commentRepository.findById(request.getParentCommentId())
                    .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

            if (parent.isReply()) {
                throw new CustomException(ErrorCode.INVALID_COMMENT);
            }

            if (!parent.getPost().getPostId().equals(post.getPostId())) {
                throw new CustomException(ErrorCode.INVALID_COMMENT);
            }
        }

        Comment comment = Comment.builder()
                .post(post)
                .user(user)
                .parentComment(parent)
                .visibility(request.getVisibility())
                .content(request.getContent())
                .build();

        commentRepository.save(comment);

        if (parent != null) {
            parent.addChildComment(comment);
        }

        post.increaseCommentCount();

        return CommentCommandResponse.builder()
                .commentId(comment.getCommentId())
                .postId(post.getPostId())
                .parentCommentId(comment.getParentComment().getCommentId())
                .userId(user.getUserId())
                .content(comment.getContent())
                .visibility(comment.getVisibility())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .commentCount(post.getCommentCount())
                .build();
    }

    @Transactional
    public CommentCommandResponse updateComment(Long commentId, User user, CommentUpdateRequest request) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isOwner(user.getUserId()) && !user.isAdmin()) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        comment.updateContent(request.getContent());
        comment.changeVisibility(request.getVisibility());

        return CommentCommandResponse.builder()
                .commentId(comment.getCommentId())
                .postId(comment.getPost().getPostId())
                .parentCommentId(comment.getParentComment().getCommentId())
                .userId(user.getUserId())
                .content(comment.getContent())
                .visibility(comment.getVisibility())
                .isDeleted(false)
                .createdAt(LocalDateTime.now())
                .build();
    }

    @Transactional
    public CommentDeleteResponse deleteComment(Long commentId, User user) {
        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new CustomException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.isOwner(user.getUserId()) && !user.isAdmin()) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }

        if (comment.isReply()) {
            commentRepository.delete(comment);
            comment.getPost().decreaseCommentCount();

            return CommentDeleteResponse.builder()
                    .commentId(commentId)
                    .softDeleted(false)
                    .commentCount(comment.getPost().getCommentCount())
                    .build();
        }

        boolean hasChildren =
                commentRepository.existsByParentComment_CommentId(comment.getCommentId());

        if (!hasChildren) {
            commentRepository.delete(comment);
            comment.getPost().decreaseCommentCount();

            return CommentDeleteResponse.builder()
                    .commentId(commentId)
                    .softDeleted(false)
                    .commentCount(comment.getPost().getCommentCount())
                    .build();
        } else {
            comment.softDelete();

            return CommentDeleteResponse.builder()
                    .commentId(commentId)
                    .softDeleted(false)
                    .commentCount(comment.getPost().getCommentCount())
                    .build();
        }
    }

    private void validatePostCommentPermission(Post post, User user) {

        if (post.getVisibility() == Visibility.PRIVATE && !post.isOwner(user.getUserId())) {
            throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
        }
    }
}
