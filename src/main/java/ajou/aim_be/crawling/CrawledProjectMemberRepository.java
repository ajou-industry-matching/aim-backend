package ajou.aim_be.crawling;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CrawledProjectMemberRepository
        extends JpaRepository<CrawledProjectMember, Long> {

    List<CrawledProjectMember>
    findByProject_CrawledProjectId(Long projectId);

    List<CrawledProjectMember>
    findByNameAndEmailPrefix(
            String name,
            String emailPrefix
    );

    @Query("""
        SELECT m
        FROM CrawledProjectMember m
        JOIN FETCH m.project p
        WHERE m.name = :name
          AND :email LIKE CONCAT(m.emailPrefix, '%@ajou.ac.kr')
        """)
    List<CrawledProjectMember> findMyProjects(
            @Param("name") String name,
            @Param("email") String email
    );

    @Query("""
        SELECT COUNT(m) > 0
        FROM CrawledProjectMember m
        WHERE m.project.crawledProjectId = :projectId
          AND m.name = :name
          AND :email LIKE CONCAT(m.emailPrefix, '%@ajou.ac.kr')
        """)
    boolean existsMyMembership(
            @Param("projectId") Long projectId,
            @Param("name") String name,
            @Param("email") String email
    );
}