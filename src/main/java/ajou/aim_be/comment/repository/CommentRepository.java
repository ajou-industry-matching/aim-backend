package ajou.aim_be.comment.repository;

import ajou.aim_be.comment.Comment;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    List<Comment> findByPost_PostIdAndParentCommentIsNullOrderByCreatedAtAsc(Long postId);

    List<Comment> findByParentComment_CommentIdInOrderByCreatedAtAsc(List<Long> parentIds);

    Page<Comment> findByPost_PostIdAndParentCommentIsNull(
            Long postId,
            Pageable pageable
    );

    boolean existsByParentComment_CommentId(Long parentId);

}