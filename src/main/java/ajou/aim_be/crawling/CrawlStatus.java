package ajou.aim_be.crawling;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "CRAWL_STATUS")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class CrawlStatus {

    @Id
    private Long id;

    @Column(name = "last_crawled_at")
    private LocalDateTime lastCrawledAt;

    public void updateLastCrawledAt(LocalDateTime time) {
        this.lastCrawledAt = time;
    }
}