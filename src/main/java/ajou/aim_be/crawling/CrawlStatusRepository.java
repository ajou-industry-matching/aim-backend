package ajou.aim_be.crawling;

import org.springframework.data.jpa.repository.JpaRepository;

public interface CrawlStatusRepository extends JpaRepository<CrawlStatus, Long> {
}