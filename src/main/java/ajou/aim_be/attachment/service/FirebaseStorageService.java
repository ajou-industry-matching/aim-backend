package ajou.aim_be.attachment.service;

import ajou.aim_be.attachment.dto.FirebaseFileUploadResult;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import com.google.cloud.storage.Blob;
import com.google.cloud.storage.Bucket;
import com.google.firebase.cloud.StorageClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FirebaseStorageService {

    private static final long MAX_FILE_SIZE = 20 * 1024 * 1024;

    public FirebaseFileUploadResult uploadPostFile(MultipartFile file, Long postId, Long order) {
        validateFile(file);

        String originalFilename = file.getOriginalFilename();
        String extension = extractExtension(originalFilename);
        String storedFilename = UUID.randomUUID() + (extension.isBlank() ? "" : "." + extension);

        String storageKey = "posts/" + postId + "/" + order + "_" + storedFilename;

        try {
            Bucket bucket = StorageClient.getInstance().bucket();

            Blob blob = bucket.create(
                    storageKey,
                    file.getBytes(),
                    file.getContentType()
            );

            String encodedPath = URLEncoder.encode(blob.getName(), StandardCharsets.UTF_8)
                    .replace("+", "%20");

            String fileUrl = String.format(
                    "https://firebasestorage.googleapis.com/v0/b/%s/o/%s?alt=media",
                    bucket.getName(),
                    encodedPath
            );

            return FirebaseFileUploadResult.builder()
                    .originalFilename(originalFilename)
                    .storedFilename(storedFilename)
                    .fileUrl(fileUrl)
                    .storageKey(storageKey)
                    .contentType(file.getContentType())
                    .fileSize(file.getSize())
                    .build();

        } catch (IOException e) {
            throw new CustomException(ErrorCode.FILE_UPLOAD_FAILED);
        }
    }

    public void deleteFile(String storageKey) {
        if (storageKey == null || storageKey.isBlank()) {
            return;
        }

        Bucket bucket = StorageClient.getInstance().bucket();
        Blob blob = bucket.get(storageKey);

        if (blob != null) {
            blob.delete();
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new CustomException(ErrorCode.INVALID_FILE);
        }

        if (file.getSize() > MAX_FILE_SIZE) {
            throw new CustomException(ErrorCode.FILE_SIZE_EXCEEDED);
        }
    }

    private String extractExtension(String filename) {
        if (filename == null || !filename.contains(".")) {
            return "";
        }
        return filename.substring(filename.lastIndexOf('.') + 1);
    }
}