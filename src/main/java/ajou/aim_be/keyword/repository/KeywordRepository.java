package ajou.aim_be.keyword.repository;

import ajou.aim_be.keyword.Keyword;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface KeywordRepository extends JpaRepository<Keyword, Long> {

    List<Keyword> findByKeywordIdIn(List<Long> keywordIds);

    List<Keyword> findAllByOrderByKeywordNameAsc();

    List<Keyword> findByKeywordNameContainingIgnoreCaseOrderByKeywordNameAsc(String keywordName);

    Optional<Keyword> findByKeywordNameIgnoreCase(String keywordName);

    boolean existsByKeywordNameIgnoreCase(String keywordName);

}