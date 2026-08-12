package ajou.aim_be.keyword.repository;

import ajou.aim_be.keyword.PostKeyword;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface PostKeywordRepository extends JpaRepository<PostKeyword, Long> {

    List<PostKeyword> findByPost_PostId(Long postId);

    void deleteByPost_PostId(Long postId);

    List<PostKeyword> findByPost_PostIdIn(List<Long> postIds);

    List<PostKeyword> findByPost_PostIdInOrderByPost_PostIdAscPostKeywordIdAsc(List<Long> postIds);

    void deleteByKeyword_KeywordId(Long keywordId);

    @Query("""
        SELECT pk
        FROM PostKeyword pk
        JOIN FETCH pk.keyword
        WHERE pk.post.postId IN :postIds
        ORDER BY pk.post.postId ASC, pk.postKeywordId ASC
    """)
    List<PostKeyword> findAllWithKeywordByPostIds(
            @Param("postIds") List<Long> postIds
    );

}