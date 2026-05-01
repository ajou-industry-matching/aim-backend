package ajou.aim_be.keyword.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Builder
@Getter
@Schema(description = "키워드 삭제 응답 DTO")
public class DeleteKeywordResponse {

    @Schema(description = "삭제된 키워드 ID", example = "5")
    private Long keywordId;
}