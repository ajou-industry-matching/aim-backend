package ajou.aim_be.post.dto;

import ajou.aim_be.attachment.Attachment;
import ajou.aim_be.attachment.AttachmentType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@Schema(description = "첨부파일 응답 DTO")
public class AttachmentResponse {

    @Schema(description = "첨부파일 ID", example = "1")
    private Long attachmentId;

    @Schema(description = "첨부파일 타입", example = "IMAGE")
    private AttachmentType attachmentType;

    @Schema(description = "원본 파일명", example = "profile.png")
    private String originalFilename;

    @Schema(description = "파일 경로 또는 URL")
    private String filePath;

    @Schema(description = "파일 MIME 타입", example = "image/png")
    private String fileType;

    @Schema(description = "파일 크기(bytes)")
    private Long fileSize;

    @Schema(description = "노출 순서", example = "1")
    private Long displayOrder;

    @Schema(description = "이미지 여부", example = "true")
    private boolean image;

    public static AttachmentResponse from(Attachment attachment) {
        return AttachmentResponse.builder()
                .attachmentId(attachment.getAttachmentId())
                .attachmentType(attachment.getType())
                .originalFilename(attachment.getOriginalFilename())
                .filePath(attachment.getFilePath())
                .fileType(attachment.getFileType())
                .fileSize(attachment.getFileSize())
                .displayOrder(attachment.getDisplayOrder())
                .image(attachment.isImage())
                .build();
    }
}
