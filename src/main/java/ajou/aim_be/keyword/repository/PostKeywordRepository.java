package ajou.aim_be.keyword.repository;

import ajou.aim_be.keyword.PostKeyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface PostKeywordRepository extends JpaRepository<PostKeyword, Long> {

    List<PostKeyword> findByPost_PostId(Long postId);

    void deleteByPost_PostId(Long postId);

    List<PostKeyword> findByPost_PostIdIn(List<Long> postIds);

    void deleteByKeyword_KeywordId(Long keywordId);

}