package ajou.aim_be.keyword.controller;

import ajou.aim_be.keyword.service.KeywordService;
import ajou.aim_be.post.dto.KeywordResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Tag(name = "Keyword", description = "키워드 조회 API")
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/keywords")
public class KeywordController {

    private final KeywordService keywordService;

    @Operation(summary = "전체 키워드 조회", description = "등록된 전체 키워드 목록을 조회합니다.")
    @GetMapping
    public List<KeywordResponse> getKeywords() {
        return keywordService.getKeywords();
    }

    @Operation(summary = "키워드 검색", description = "키워드명으로 키워드를 검색합니다.")
    @GetMapping("/search")
    public List<KeywordResponse> searchKeywords(
            @RequestParam(required = false) String keyword
    ) {
        return keywordService.searchKeywords(keyword);
    }
}