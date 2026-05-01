package ajou.aim_be.post.repository;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.post.Post;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostRepository extends JpaRepository<Post, Long>, JpaSpecificationExecutor<Post> {

    Page<Post> findByBoardTypeAndVisibility(
            BoardType boardType,
            Visibility visibility,
            Pageable pageable
    );

    Page<Post> findByUser_UserId(
            Long userId,
            Pageable pageable
    );

    Page<Post> findByUser_UserIdAndVisibility(
            Long userId,
            Visibility visibility,
            Pageable pageable
    );

//    @Query("""
//    SELECT DISTINCT p FROM Post p
//    LEFT JOIN PostKeyword pk ON pk.post = p
//    LEFT JOIN Keyword k ON pk.keyword = k
//    WHERE p.boardType = :boardType
//    AND p.visibility = ajou.aim_be.global.common.Visibility.PUBLIC
//    AND (
//        LOWER(p.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
//        OR LOWER(p.description) LIKE LOWER(CONCAT('%', :keyword, '%'))
//        OR LOWER(p.content) LIKE LOWER(CONCAT('%', :keyword, '%'))
//        OR LOWER(k.keywordName) LIKE LOWER(CONCAT('%', :keyword, '%'))
//    )
//    """)
//    Page<Post> searchPosts(
//            @Param("boardType") BoardType boardType,
//            @Param("keyword") String keyword,
//            Pageable pageable
//    );

    Page<Post> findByVisibility(
            Visibility visibility,
            Pageable pageable
    );

    List<Post> findTop4ByBoardTypeAndVisibilityOrderByCreatedAtDesc(
            BoardType boardType,
            Visibility visibility
    );
}