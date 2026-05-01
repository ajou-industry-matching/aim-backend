package ajou.aim_be.post.service;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.keyword.repository.PostKeywordRepository;
import ajou.aim_be.like.Like;
import ajou.aim_be.like.repository.LikeRepository;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.dto.*;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.post.repository.PostSpecification;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PostQueryService {

    private final PostRepository postRepository;
    private final PostResponseAssembler assembler;
    private final LikeRepository likeRepository;
    private final PostKeywordRepository postKeywordRepository;

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getMyPosts(User user, Pageable pageable) {

        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Page<Post> page = postRepository.findByUser_UserId(
                user.getUserId(),
                pageable
        );

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.from(Page.empty());
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        Set<Long> likedSet = new HashSet<>(
                likeRepository.findLikedPostIds(user.getUserId(), postIds)
        );

        Map<Long, List<KeywordResponse>> keywordMap = buildKeywordMap(postIds);


        List<PostListResponse> responses = posts.stream()
                .map(post -> assembler.assemble(post, likedSet, keywordMap))
                .toList();

        return PageResponse.<PostListResponse>builder()
                .content(responses)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getPosts(BoardType boardType, Pageable pageable, User user) {

        Page<Post> page = postRepository
                .findByBoardTypeAndVisibility(
                        boardType,
                        Visibility.PUBLIC,
                        pageable
                );

        List<Post> posts = page.getContent();
        if (posts.isEmpty()) {
            return PageResponse.from(Page.empty());
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        Set<Long> likedSet =
                (user == null)
                        ? Collections.emptySet()
                        : new HashSet<>(likeRepository.findLikedPostIds(user.getUserId(), postIds));

        Map<Long, List<KeywordResponse>> keywordMap = buildKeywordMap(postIds);

        List<PostListResponse> responses = posts.stream()
                .map(post -> assembler.assemble(post, likedSet, keywordMap))
                .toList();

        return PageResponse.<PostListResponse>builder()
                .content(responses)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional
    public PostDetailResponse getPost(BoardType boardType, Long postId, User user) {
        Post post = postRepository.findById(postId)
                .orElseThrow(() -> new CustomException(ErrorCode.POST_NOT_FOUND));

        if (post.getBoardType() != boardType) {
            throw new CustomException(ErrorCode.BOARD_MISMATCH);
        }

        if (post.getVisibility() == Visibility.PRIVATE) {
            if (user == null || !post.isOwner(user.getUserId())) {
                throw new CustomException(ErrorCode.POST_ACCESS_DENIED);
            }
        }

        post.increaseViewCount();

        return assembler.assemble(post,
                user != null ? user.getUserId() : null
        );
    }

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getLikedPosts(User user, Pageable pageable) {

        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Page<Like> likePage = likeRepository
                .findByUser_UserIdAndPost_VisibilityOrderByCreatedAtDesc(user.getUserId(), Visibility.PUBLIC, pageable);

        List<Post> posts = likePage.getContent()
                .stream()
                .map(Like::getPost)
                .filter(post -> post.getVisibility() == Visibility.PUBLIC)
                .toList();

        if (posts.isEmpty()) {
            return PageResponse.from(Page.empty());
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        Set<Long> likedSet = new HashSet<>(postIds);

        Map<Long, List<KeywordResponse>> keywordMap = buildKeywordMap(postIds);

        List<PostListResponse> responses = posts.stream()
                .map(post -> assembler.assemble(post, likedSet, keywordMap))
                .toList();

        return PageResponse.<PostListResponse>builder()
                .content(responses)
                .page(likePage.getNumber())
                .size(likePage.getSize())
                .totalElements(likePage.getTotalElements())
                .totalPages(likePage.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getUserPosts(
            Long targetUserId,
            User requester,
            Pageable pageable
    ) {

        Page<Post> page;

        if (requester != null &&
                (requester.getUserId().equals(targetUserId) || requester.isAdmin())) {

            page = postRepository.findByUser_UserId(
                    targetUserId,
                    pageable
            );

        } else {
            page = postRepository
                    .findByUser_UserIdAndVisibility(
                            targetUserId,
                            Visibility.PUBLIC,
                            pageable
                    );
        }

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.from(Page.empty());
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        Set<Long> likedSet =
                (requester == null)
                        ? Collections.emptySet()
                        : new HashSet<>(
                        likeRepository.findLikedPostIds(requester.getUserId(), postIds)
                );

        Map<Long, List<KeywordResponse>> keywordMap = buildKeywordMap(postIds);

        List<PostListResponse> responses = posts.stream()
                .map(post -> assembler.assemble(post, likedSet, keywordMap))
                .toList();

        return PageResponse.<PostListResponse>builder()
                .content(responses)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> searchPosts(
            BoardType boardType,
            String keyword,
            User user,
            Pageable pageable
    ) {

        if (keyword == null || keyword.trim().isEmpty()) {
            return getPosts(boardType, pageable, user);
        }

        List<String> keywords = parseKeywords(keyword);

        Specification<Post> spec = PostSpecification.base(boardType)
                .and(PostSpecification.keywordOr(keywords));

        Page<Post> page = postRepository.findAll(spec, pageable);

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.from(Page.empty());
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        Set<Long> likedSet =
                (user == null)
                        ? Collections.emptySet()
                        : new HashSet<>(
                        likeRepository.findLikedPostIds(user.getUserId(), postIds)
                );

        Map<Long, List<KeywordResponse>> keywordMap = buildKeywordMap(postIds);

        List<PostListResponse> responses = posts.stream()
                .map(post -> assembler.assemble(post, likedSet, keywordMap))
                .toList();

        return PageResponse.<PostListResponse>builder()
                .content(responses)
                .page(page.getNumber())
                .size(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .build();
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getRecentPosts(User user) {

        Pageable pageable = PageRequest.of(0, 4, Sort.by(Sort.Direction.DESC, "createdAt"));

        Page<Post> page = postRepository.findByVisibility(
                Visibility.PUBLIC,
                pageable
        );

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return List.of();
        }

        List<Long> postIds = posts.stream()
                .map(Post::getPostId)
                .toList();

        Set<Long> likedSet =
                (user == null)
                        ? Collections.emptySet()
                        : new HashSet<>(
                        likeRepository.findLikedPostIds(user.getUserId(), postIds)
                );

        Map<Long, List<KeywordResponse>> keywordMap = buildKeywordMap(postIds);

        return posts.stream()
                .map(post -> assembler.assemble(post, likedSet, keywordMap))
                .toList();
    }

    @Transactional(readOnly = true)
    public List<PostListResponse> getNoticesPosts() {

        List<Post> posts = postRepository.findTop4ByBoardTypeAndVisibilityOrderByCreatedAtDesc(
                BoardType.NOTICE,
                Visibility.PUBLIC
        );

        return posts.stream()
                .map(post -> PostListResponse.builder()
                        .postId(post.getPostId())
                        .title(post.getTitle())
                        .description(post.getDescription())
                        .thumbnailImage(post.getThumbnailImage())
                        .viewCount(post.getViewCount())
                        .commentCount(post.getCommentCount())
                        .likeCount(post.getLikeCount())
                        .createdAt(post.getCreatedAt())
                        .keywords(List.of())
                        .boardType(BoardType.NOTICE)
                        .visibility(Visibility.PUBLIC)
                        .liked(false)
                        .build())
                .toList();
    }

    private Map<Long, List<KeywordResponse>> buildKeywordMap(List<Long> postIds) {

        return postKeywordRepository.findByPost_PostIdIn(postIds)
                .stream()
                .collect(Collectors.groupingBy(
                        pk -> pk.getPost().getPostId(),
                        Collectors.mapping(
                                pk -> KeywordResponse.from(pk.getKeyword()),
                                Collectors.toList()
                        )
                ));
    }

    private List<String> parseKeywords(String keyword) {
        return Arrays.stream(keyword.trim().split("\\s+"))
                .filter(s -> !s.isBlank())
                .distinct()
                .toList();
    }
}