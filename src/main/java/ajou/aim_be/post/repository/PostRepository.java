package ajou.aim_be.post.repository;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByBoardTypeAndVisibility(
            BoardType boardType,
            Visibility visibility,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "user")
    Page<Post> findByBoardTypeInAndVisibility(
            List<BoardType> boardTypes,
            Visibility visibility,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "user")
    Page<Post> findByUser_UserId(
            Long userId,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "user")
    Page<Post> findByUser_UserIdAndVisibility(
            Long userId,
            Visibility visibility,
            Pageable pageable
    );

    long countByUser_UserId(Long userId);

    @EntityGraph(attributePaths = "user")
    Page<Post> findByVisibility(
            Visibility visibility,
            Pageable pageable
    );

    @EntityGraph(attributePaths = "user")
    List<Post> findTop4ByBoardTypeAndVisibilityOrderByCreatedAtDesc(
            BoardType boardType,
            Visibility visibility
    );

    List<Post> findByUser_UserIdAndCrawledProjectIdIn(
            Long userId,
            Collection<Long> crawledProjectIds
    );

    Optional<Post> findFirstByUser_UserIdAndCrawledProjectIdOrderByPostIdAsc(
            Long userId,
            Long crawledProjectId
    );

    @EntityGraph(attributePaths = "user")
    List<Post> findByVisibility(Visibility visibility);
}