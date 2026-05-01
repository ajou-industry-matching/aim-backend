package ajou.aim_be.attachment.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "파일 업로드 결과")
public class FirebaseFileUploadResult {

    @Schema(description = "원본 파일명")
    private String originalFilename;

    @Schema(description = "저장된 파일명")
    private String storedFilename;

    @Schema(description = "파일 URL")
    private String fileUrl;

    @Schema(description = "스토리지 키")
    private String storageKey;

    @Schema(description = "파일 타입")
    private String contentType;

    @Schema(description = "파일 크기")
    private Long fileSize;
}