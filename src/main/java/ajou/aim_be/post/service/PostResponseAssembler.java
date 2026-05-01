package ajou.aim_be.post.service;

import ajou.aim_be.attachment.Attachment;
import ajou.aim_be.attachment.AttachmentType;
import ajou.aim_be.attachment.repository.AttachmentRepository;
import ajou.aim_be.keyword.PostKeyword;
import ajou.aim_be.keyword.repository.PostKeywordRepository;
import ajou.aim_be.like.repository.LikeRepository;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.dto.AttachmentResponse;
import ajou.aim_be.post.dto.KeywordResponse;
import ajou.aim_be.post.dto.PostListResponse;
import ajou.aim_be.post.dto.PostDetailResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;
import java.util.Set;

@Component
@RequiredArgsConstructor
public class PostResponseAssembler {

    private final AttachmentRepository attachmentRepository;
    private final PostKeywordRepository postKeywordRepository;
    private final LikeRepository likeRepository;

    public PostListResponse assemble(Post post, Set<Long> likedSet, Map<Long, List<KeywordResponse>> keywordMap) {

        List<KeywordResponse> keywords =
                keywordMap.getOrDefault(post.getPostId(), List.of());

        return PostListResponse.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getUserId())
                .boardType(post.getBoardType())
                .title(post.getTitle())
                .description(post.getDescription())
                .thumbnailImage(post.getThumbnailImage())
                .likeCount(post.getLikeCount())
                .commentCount(post.getCommentCount())
                .liked(likedSet.contains(post.getPostId()))
                .createdAt(post.getCreatedAt())
                .visibility(post.getVisibility())
                .viewCount(post.getViewCount())
                .commentCount(post.getCommentCount())
                .keywords(keywords)
                .build();
    }

    public PostDetailResponse assemble(Post post, Long userId) {

        List<KeywordResponse> keywords = postKeywordRepository.findByPost_PostId(post.getPostId())
                .stream()
                .map(PostKeyword::getKeyword)
                .map(KeywordResponse::from)
                .toList();

        List<Attachment> attachments =
                attachmentRepository.findByPost_PostIdOrderByDisplayOrderAscCreatedAtAsc(post.getPostId());

        List<AttachmentResponse> images = attachments.stream()
                .filter(a -> a.getType() == AttachmentType.IMAGE)
                .map(AttachmentResponse::from)
                .toList();

        List<AttachmentResponse> files = attachments.stream()
                .filter(a -> a.getType() == AttachmentType.FILE)
                .map(AttachmentResponse::from)
                .toList();

        boolean liked = false;
        if (userId != null) {
            liked = likeRepository.existsByPost_PostIdAndUser_UserId(post.getPostId(), userId);
        }

        return PostDetailResponse.builder()
                .postId(post.getPostId())
                .userId(post.getUser().getUserId())
                .boardType(post.getBoardType())
                .title(post.getTitle())
                .description(post.getDescription())
                .content(post.getContent())
                .githubLink(post.getGithubLink())
                .videoLink(post.getVideoLink())
                .likeCount(post.getLikeCount())
                .liked(liked)
                .visibility(post.getVisibility())
                .viewCount(post.getViewCount())
                .thumbnailImage(post.getThumbnailImage())
                .commentCount(post.getCommentCount())
                .createdAt(post.getCreatedAt())
                .keywords(keywords)
                .images(images)
                .files(files)
                .build();
    }
}