package ajou.aim_be.like.repository;

import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.like.Like;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface LikeRepository extends JpaRepository<Like, Long> {

    @Query("""
        SELECT l.post.postId
        FROM Like l
        WHERE l.user.userId = :userId
        AND l.post.postId IN :postIds
    """)
    List<Long> findLikedPostIds(Long userId, List<Long> postIds);

    boolean existsByPost_PostIdAndUser_UserId(Long postId, Long userId);

    Page<Like> findByUser_UserIdOrderByCreatedAtDesc(Long userId, Pageable pageable);

    Optional<Like> findByPost_PostIdAndUser_UserId(Long postId, Long userId);

    void deleteByPost_PostIdAndUser_UserId(Long postId, Long userId);

    // 좋아요 목록용
    Page<Like> findByUser_UserIdAndPost_VisibilityOrderByCreatedAtDesc(Long userId, Visibility visibility, Pageable pageable);

}