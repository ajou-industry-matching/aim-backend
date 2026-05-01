package ajou.aim_be.post.service;

import ajou.aim_be.attachment.Attachment;
import ajou.aim_be.attachment.AttachmentType;
import ajou.aim_be.attachment.dto.FirebaseFileUploadResult;
import ajou.aim_be.attachment.repository.AttachmentRepository;
import ajou.aim_be.attachment.service.FirebaseStorageService;
import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.policy.PostPermissionPolicy;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.keyword.Keyword;
import ajou.aim_be.keyword.PostKeyword;
import ajou.aim_be.keyword.repository.KeywordRepository;
import ajou.aim_be.keyword.repository.PostKeywordRepository;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.dto.DeletePostResponse;
import ajou.aim_be.post.dto.PostCreateRequest;
import ajou.aim_be.post.dto.PostDetailResponse;
import ajou.aim_be.post.dto.PostUpdateRequest;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostCommandService {

    private static final int MAX_KEYWORD_COUNT = 10;

    private final PostRepository postRepository;
    private final KeywordRepository keywordRepository;
    private final PostKeywordRepository postKeywordRepository;
    private final AttachmentRepository attachmentRepository;
    private final FirebaseStorageService firebaseStorageService;
    private final PostResponseAssembler postResponseAssembler;

    @Transactional
    public PostDetailResponse createPost(
            BoardType boardType,
            User user,
            PostCreateRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> images,
            List<MultipartFile> files
    ) {
        PostPermissionPolicy.validateCreatePermission(boardType, user);

        Visibility visibility = request.getVisibility() == null
                ? Visibility.PUBLIC
                : request.getVisibility();

        Post post = Post.builder()
                .user(user)
                .boardType(boardType)
                .title(request.getTitle())
                .description(request.getDescription())
                .content(request.getContent())
                .videoLink(request.getVideoLink())
                .githubLink(request.getGithubLink())
                .visibility(visibility)
                .build();

        postRepository.save(post);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            FirebaseFileUploadResult result = uploadThumbnail(thumbnail, post.getPostId());

            post.updateThumbnail(
                    result.getFileUrl(),
                    result.getStorageKey()
            );
        }

        validateCreateAttachmentCount(images, files);

        savePostKeywords(post, request.getKeywordIds());
        saveAttachments(post, images, AttachmentType.IMAGE);
        saveAttachments(post, files, AttachmentType.FILE);

        return postResponseAssembler.assemble(post, user.getUserId());
    }

    @Transactional
    public PostDetailResponse updatePost(
            BoardType boardType,
            Long postId,
            User user,
            PostUpdateRequest request,
            MultipartFile thumbnail,
            List<MultipartFile> images,
            List<MultipartFile> files
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validateBoardAndPermission(boardType, user, post);

        post.update(request.getTitle(), request.getDescription(), request.getContent(), request.getGithubLink(), request.getVideoLink());

        if (request.getVisibility() != null) {
            post.changeVisibility(request.getVisibility());
        }

        if (thumbnail != null && !thumbnail.isEmpty()) {

            if (post.getThumbnailStorageKey() != null) {
                firebaseStorageService.deleteFile(post.getThumbnailStorageKey());
            }

            FirebaseFileUploadResult result = uploadThumbnail(thumbnail, post.getPostId());

            if (result != null) {
                post.updateThumbnail(
                        result.getFileUrl(),
                        result.getStorageKey()
                );
            }
        }

        validateUpdateAttachmentCount(post, request.getDeleteAttachmentIds(), images, files);

        replacePostKeywords(post, request.getKeywordIds());
        deleteSelectedAttachments(post, request.getDeleteAttachmentIds());
        saveAttachments(post, images, AttachmentType.IMAGE);
        saveAttachments(post, files, AttachmentType.FILE);

        return postResponseAssembler.assemble(post, user.getUserId());
    }

    @Transactional
    public DeletePostResponse deletePost(
            BoardType boardType,
            Long postId,
            User user
    ) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        validateBoardAndPermission(boardType, user, post);

        if (post.getThumbnailStorageKey() != null) {
            firebaseStorageService.deleteFile(post.getThumbnailStorageKey());
        }

        List<Attachment> attachments = attachmentRepository.findByPost_PostIdOrderByDisplayOrderAscCreatedAtAsc(postId);
        for (Attachment attachment : attachments) {
            firebaseStorageService.deleteFile(attachment.getStorageKey());
        }

        postKeywordRepository.deleteByPost_PostId(postId);
        attachmentRepository.deleteByPost_PostId(postId);
        postRepository.delete(post);

        return DeletePostResponse.builder()
                .postId(postId)
                .build();
    }

    private void validateBoardAndPermission(BoardType boardType, User user, Post post) {
        if (post.getBoardType() != boardType) {
            throw new CustomException(ErrorCode.BOARD_MISMATCH);
        }

        if (!post.isOwner(user.getUserId()) && !user.isAdmin()) {
            throw new CustomException(ErrorCode.NO_PERMISSION);
        }
    }

    private void savePostKeywords(Post post, List<Long> keywordIds) {
        if (keywordIds == null || keywordIds.isEmpty()) {
            return;
        }

        validateKeywordIds(keywordIds);

        List<Keyword> keywords = keywordRepository.findByKeywordIdIn(keywordIds);

        if (keywords.size() != new HashSet<>(keywordIds).size()) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }

        for (Keyword keyword : keywords) {
            postKeywordRepository.save(
                    PostKeyword.builder()
                            .post(post)
                            .keyword(keyword)
                            .build()
            );
        }
    }

    private void replacePostKeywords(Post post, List<Long> keywordIds) {
        postKeywordRepository.deleteByPost_PostId(post.getPostId());
        savePostKeywords(post, keywordIds);
    }

    private void saveAttachments(Post post, List<MultipartFile> files, AttachmentType type) {
        List<MultipartFile> safeFiles = files == null ? Collections.emptyList() : files;
        if (safeFiles.isEmpty()) {
            return;
        }

        List<Attachment> existingAttachments = attachmentRepository.findByPost_PostIdOrderByDisplayOrderAscCreatedAtAsc(post.getPostId());

        long startOrder = existingAttachments.stream()
                .mapToLong(Attachment::getDisplayOrder)
                .max()
                .orElse(-1) + 1;

        for (int i = 0; i < safeFiles.size(); i++) {
            MultipartFile file = safeFiles.get(i);

            FirebaseFileUploadResult uploadResult =
                    firebaseStorageService.uploadPostFile(file, post.getPostId(), startOrder + i);

            Attachment attachment = Attachment.builder()
                    .post(post)
                    .type(type)
                    .originalFilename(uploadResult.getOriginalFilename())
                    .storedFilename(uploadResult.getStoredFilename())
                    .filePath(uploadResult.getFileUrl())
                    .storageKey(uploadResult.getStorageKey())
                    .fileType(uploadResult.getContentType())
                    .fileSize(uploadResult.getFileSize())
                    .displayOrder(startOrder + i)
                    .build();

            attachmentRepository.save(attachment);
        }
    }

    private FirebaseFileUploadResult uploadThumbnail(MultipartFile thumbnail, Long postId) {

        if (thumbnail == null || thumbnail.isEmpty()) {
            return null;
        }

        return firebaseStorageService.uploadPostFile(thumbnail, postId, 0L);

    }

    private void deleteSelectedAttachments(Post post, List<Long> deleteAttachmentIds) {
        if (deleteAttachmentIds == null || deleteAttachmentIds.isEmpty()) {
            return;
        }

        List<Attachment> attachments = attachmentRepository.findByAttachmentIdIn(deleteAttachmentIds);

        for (Attachment attachment : attachments) {
            if (!attachment.belongsTo(post.getPostId())) {
                throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
            }
        }

        for (Attachment attachment : attachments) {
            firebaseStorageService.deleteFile(attachment.getStorageKey());
            attachmentRepository.delete(attachment);
        }
    }

    private void validateKeywordIds(List<Long> keywordIds) {
        Set<Long> uniqueIds = new HashSet<>(keywordIds);

        if (uniqueIds.size() > MAX_KEYWORD_COUNT) {
            throw new CustomException(ErrorCode.KEYWORD_LIMIT_EXCEEDED);
        }

        if (uniqueIds.size() != keywordIds.size()) {
            throw new CustomException(ErrorCode.INVALID_KEYWORD);
        }
    }

    private static final int MAX_ATTACHMENT_COUNT = 5;

    private void validateCreateAttachmentCount(
            List<MultipartFile> imageFiles,
            List<MultipartFile> files
    ) {
        int newCount = safeSize(imageFiles) + safeSize(files);
        if (newCount > MAX_ATTACHMENT_COUNT) {
            throw new CustomException(ErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }
    }

    private void validateUpdateAttachmentCount(
            Post post,
            List<Long> deleteAttachmentIds,
            List<MultipartFile> imageFiles,
            List<MultipartFile> files
    ) {
        List<Attachment> existingAttachments =
                attachmentRepository.findByPost_PostIdOrderByDisplayOrderAsc(post.getPostId());

        Set<Long> existingIds = existingAttachments.stream()
                .map(Attachment::getAttachmentId)
                .collect(Collectors.toSet());

        Set<Long> deleteIds = deleteAttachmentIds == null
                ? Set.of()
                : new HashSet<>(deleteAttachmentIds);

        if (!existingIds.containsAll(deleteIds)) {
            throw new CustomException(ErrorCode.INVALID_ATTACHMENT);
        }

        int remainingCount = existingAttachments.size() - deleteIds.size();
        int newCount = safeSize(imageFiles) + safeSize(files);

        if (remainingCount + newCount > MAX_ATTACHMENT_COUNT) {
            throw new CustomException(ErrorCode.ATTACHMENT_LIMIT_EXCEEDED);
        }
    }

    private int safeSize(List<?> list) {
        return list == null ? 0 : list.size();
    }
}