package ajou.aim_be.post.dto;

import ajou.aim_be.keyword.Keyword;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "키워드 응답 DTO")
public class KeywordResponse {

    @Schema(description = "키워드 ID")
    private Long keywordId;

    @Schema(description = "키워드명", example = "백엔드")
    private String keywordName;

    public static KeywordResponse from(Keyword keyword) {
        return KeywordResponse.builder()
                .keywordId(keyword.getKeywordId())
                .keywordName(keyword.getKeywordName())
                .build();
    }
}