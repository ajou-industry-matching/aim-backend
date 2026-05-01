package ajou.aim_be.keyword.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@Schema(description = "키워드 생성/수정 요청 DTO")
public class KeywordCreateRequest {

    @Schema(description = "키워드명", requiredMode = Schema.RequiredMode.REQUIRED, example = "인공지능")
    private String keywordName;
}