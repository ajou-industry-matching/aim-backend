package ajou.aim_be.keyword.service;

import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.keyword.Keyword;
import ajou.aim_be.keyword.dto.DeleteKeywordResponse;
import ajou.aim_be.keyword.dto.KeywordCreateRequest;
import ajou.aim_be.keyword.repository.KeywordRepository;
import ajou.aim_be.keyword.repository.PostKeywordRepository;
import ajou.aim_be.post.dto.KeywordResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
public class KeywordService {

    private final KeywordRepository keywordRepository;
    private final PostKeywordRepository postKeywordRepository;

    public KeywordResponse createKeyword(KeywordCreateRequest request) {
        String keywordName = normalizeKeywordName(request.getKeywordName());

        if (keywordRepository.existsByKeywordNameIgnoreCase(keywordName)) {
            throw new CustomException(ErrorCode.DUPLICATE_KEYWORD);
        }

        Keyword keyword = Keyword.builder()
                .keywordName(keywordName)
                .build();

        keywordRepository.save(keyword);

        return KeywordResponse.from(keyword);
    }

    @Transactional(readOnly = true)
    public List<KeywordResponse> getKeywords() {
        return keywordRepository.findAllByOrderByKeywordNameAsc()
                .stream()
                .map(KeywordResponse::from)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<KeywordResponse> searchKeywords(String keyword) {
        String normalized = keyword == null ? "" : keyword.trim();

        if (normalized.isEmpty()) {
            return getKeywords();
        }

        return keywordRepository.findByKeywordNameContainingIgnoreCaseOrderByKeywordNameAsc(normalized)
                .stream()
                .map(KeywordResponse::from)
                .toList();
    }

    @Transactional
    public DeleteKeywordResponse deleteKeyword(Long keywordId) {

        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new CustomException(ErrorCode.KEYWORD_NOT_FOUND));

        postKeywordRepository.deleteByKeyword_KeywordId(keywordId);

        keywordRepository.delete(keyword);

        return DeleteKeywordResponse.builder()
                .keywordId(keyword.getKeywordId())
                .build();
    }

    @Transactional
    public KeywordResponse updateKeyword(Long keywordId, String keywordName) {

        Keyword keyword = keywordRepository.findById(keywordId)
                .orElseThrow(() -> new CustomException(ErrorCode.KEYWORD_NOT_FOUND));

        String normalized = normalizeKeywordName(keywordName);

        if (keywordRepository.existsByKeywordNameIgnoreCase(normalized)) {
            throw new CustomException(ErrorCode.DUPLICATE_KEYWORD);
        }

        keyword.updateName(normalized);

        return KeywordResponse.from(keyword);
    }

    private String normalizeKeywordName(String keywordName) {
        if (keywordName == null || keywordName.trim().isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }

        return keywordName.trim();
    }
}