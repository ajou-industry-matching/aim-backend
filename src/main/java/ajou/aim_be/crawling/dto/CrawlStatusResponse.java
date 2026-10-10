package ajou.aim_be.crawling.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@AllArgsConstructor
public class CrawlStatusResponse {
    private LocalDateTime lastCrawledAt;
}