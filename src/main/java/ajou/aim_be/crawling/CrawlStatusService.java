package ajou.aim_be.crawling;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class CrawlStatusService {

    private final CrawlStatusRepository repository;

    @Transactional
    public void markCompleted() {
        CrawlStatus status = repository.findById(1L)
                .orElseGet(() ->
                        CrawlStatus.builder()
                                .id(1L)
                                .build()
                );

        status.updateLastCrawledAt(LocalDateTime.now());

        repository.save(status);
    }

    @Transactional(readOnly = true)
    public LocalDateTime getLastCrawledAt() {
        return repository.findById(1L)
                .map(CrawlStatus::getLastCrawledAt)
                .orElse(null);
    }
}