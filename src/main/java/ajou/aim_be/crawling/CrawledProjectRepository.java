package ajou.aim_be.crawling;

import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Collection;
import java.util.List;
import java.util.Optional;

public interface CrawledProjectRepository
        extends JpaRepository<CrawledProject, Long> {

    Optional<CrawledProject> findBySourceUidAndTerm(
            String sourceUid,
            String term
    );

    @EntityGraph(attributePaths = "members")
    List<CrawledProject> findDistinctByCrawledProjectIdIn(
            Collection<Long> projectIds
    );

    @EntityGraph(attributePaths = "members")
    Optional<CrawledProject> findWithMembersByCrawledProjectId(
            Long projectId
    );
}