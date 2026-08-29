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

    private static final List<BoardType> FEED_BOARD_TYPES = List.of(
            BoardType.PORTFOLIO,
            BoardType.LAB_INTERN,
            BoardType.COMPANY_PROJECT
    );

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getMyPosts(User user, Pageable pageable, PostSortType sortType) {

        if (user == null) {
            throw new CustomException(ErrorCode.UNAUTHORIZED);
        }

        Pageable sortedPageable = sortType.applyTo(pageable);

        Page<Post> page = postRepository.findByUser_UserId(
                user.getUserId(),
                sortedPageable
        );

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.empty(page);
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
    public PageResponse<PostListResponse> getPosts(
            BoardType boardType,
            Pageable pageable,
            User user,
            PostSortType sortType,
            List<String> departments,
            List<Long> keywordIds
    ) {

        Pageable sortedPageable = sortType.applyTo(pageable);

        Specification<Post> spec = PostSpecification.base(boardType)
                .and(PostSpecification.departmentIn(departments))
                .and(PostSpecification.keywordIdIn(keywordIds));

        Page<Post> page = postRepository.findAll(spec, sortedPageable);

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.empty(page);
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

    @Transactional(readOnly = true)
    public PageResponse<PostListResponse> getFeedPosts(
            Pageable pageable,
            User user,
            PostSortType sortType
    ) {

        Pageable sortedPageable = sortType.applyTo(pageable);

        Page<Post> page = postRepository.findByBoardTypeInAndVisibility(
                FEED_BOARD_TYPES,
                Visibility.PUBLIC,
                sortedPageable
        );

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.empty(page);
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
//                .filter(post -> post.getVisibility() == Visibility.PUBLIC)
                .toList();

        if (posts.isEmpty()) {
            return PageResponse.empty(likePage);
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
            Pageable pageable,
            PostSortType sortType
    ) {

        Pageable sortedPageable = sortType.applyTo(pageable);

        Page<Post> page;

        if (requester != null && requester.getUserId().equals(targetUserId)) {

            page = postRepository.findByUser_UserId(
                    targetUserId,
                    sortedPageable
            );

        } else {
            page = postRepository
                    .findByUser_UserIdAndVisibility(
                            targetUserId,
                            Visibility.PUBLIC,
                            sortedPageable
                    );
        }

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.empty(page);
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
            Pageable pageable,
            PostSortType sortType,
            List<String> departments,
            List<Long> keywordIds
    ) {

        Pageable sortedPageable = sortType.applyTo(pageable);

        Specification<Post> spec = PostSpecification.base(boardType)
                .and(PostSpecification.departmentIn(departments))
                .and(PostSpecification.keywordIdIn(keywordIds));

        if (keyword != null && !keyword.trim().isEmpty()) {
            List<String> keywords = parseKeywords(keyword);
            spec = spec.and(PostSpecification.keywordOr(keywords));
        }

        Page<Post> page = postRepository.findAll(spec, sortedPageable);

        List<Post> posts = page.getContent();

        if (posts.isEmpty()) {
            return PageResponse.empty(page);
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
                        .userId(post.getUser().getUserId())
                        .authorName(post.getUser().getName())
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

                return postKeywordRepository
                .findAllWithKeywordByPostIds(postIds)
                .stream()
                .collect(Collectors.groupingBy(
                        pk -> pk.getPost().getPostId(),
                        Collectors.collectingAndThen(
                                Collectors.mapping(
                                        pk -> KeywordResponse.from(pk.getKeyword()),
                                        Collectors.toList()
                                ),
                                keywords -> keywords.stream()
                                        .limit(2)
                                        .toList()
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