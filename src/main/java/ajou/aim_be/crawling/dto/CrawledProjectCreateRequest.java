package ajou.aim_be.crawling.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

import java.util.List;

@Getter
@NoArgsConstructor
public class CrawledProjectCreateRequest {

    private String uid;
    private String term;
    private String title;

    private String summary;
    private String description;
    private String content;

    private String url;
    private String presentationUrl;
    private String videoUrl;
    private String gitRepository;
    private String representativeImage;

    private String category;

    private List<MemberRequest> members;

    @Getter
    @NoArgsConstructor
    public static class MemberRequest {

        private String role;
        private String name;
        private String maskedEmail;
        private String department;
        private String grade;
    }
}