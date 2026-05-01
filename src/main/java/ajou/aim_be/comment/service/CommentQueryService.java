package ajou.aim_be.comment.service;

import ajou.aim_be.comment.Comment;
import ajou.aim_be.comment.dto.CommentResponse;
import ajou.aim_be.comment.repository.CommentRepository;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.dto.PageResponse;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RequiredArgsConstructor
@Service
public class CommentQueryService {

    private final CommentRepository commentRepository;
    private final PostRepository postRepository;

    @Transactional(readOnly = true)
    public PageResponse<CommentResponse> getComments(Long postId, User user, Pageable pageable) {

        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validatePostReadPermission(post, user);

        Page<Comment> page =
                commentRepository.findByPost_PostIdAndParentCommentIsNull(postId, pageable);

        List<Comment> parents = page.getContent();

        if (parents.isEmpty()) {
            return PageResponse.from(Page.empty());
        }

        List<Long> parentIds = parents.stream()
                .map(Comment::getCommentId)
                .toList();

        List<Comment> children =
                commentRepository.findByParentComment_CommentIdInOrderByCreatedAtAsc(parentIds);

        Map<Long, List<Comment>> childMap =
                children.stream()
                        .collect(Collectors.groupingBy(
                                c -> c.getParentComment().getCommentId()
                        ));

        List<CommentResponse> responses = parents.stream()
                .map(parent -> CommentResponse.builder()
                        .commentId(parent.getCommentId())
                        .userId(parent.getUser().getUserId())
                        .content(resolveContent(parent,user))
                        .createdAt(parent.getCreatedAt())
                        .isDeleted(parent.isDeleted())
                        .isPrivate(parent.getVisibility() == Visibility.PRIVATE)
                        .visibility(parent.getVisibility())
                        .children(
                                childMap.getOrDefault(parent.getCommentId(), List.of())
                                        .stream()
                                        .map(child -> CommentResponse.builder()
                                                .commentId(child.getCommentId())
                                                .userId(child.getUser().getUserId())
                                                .content(resolveContent(child, user))
                                                .createdAt(child.getCreatedAt())
                                                .isDeleted(child.isDeleted())
                                                .isPrivate(child.getVisibility() == Visibility.PRIVATE)
                                                .visibility(child.getVisibility())
                                                .build()
                                        )
                                        .toList()
                        )
                        .build()
                )
                .toList();

        return PageResponse.<CommentResponse>builder()
                .content(responses)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    private void validatePostReadPermission(Post post, User user) {
        if (post.getVisibility() == Visibility.PRIVATE) {
            if (user == null || !post.isOwner(user.getUserId())) {
                throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
            }
        }
    }

    private String resolveContent(Comment comment, User user) {

        if (comment.isDeleted()) {
            return "삭제된 댓글입니다";
        }

        if (comment.getVisibility() == Visibility.PRIVATE) {

            boolean canView =
                    user != null &&
                            (
                                    comment.isOwner(user.getUserId()) ||
                                            comment.getPost().getUser().getUserId().equals(user.getUserId()) ||
                                            user.isAdmin()
                            );

            if (!canView) {
                return "비공개 댓글입니다";
            }
        }

        return comment.getContent();
    }
}
