package ajou.aim_be.post.dto;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

public enum PostSortType {

    LATEST(
            Sort.by(
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("postId")
            )
    ),

    POPULAR(
            Sort.by(
                    Sort.Order.desc("likeCount"),
                    Sort.Order.desc("commentCount"),
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("postId")
            )
    ),

    VIEWS(
            Sort.by(
                    Sort.Order.desc("viewCount"),
                    Sort.Order.desc("createdAt"),
                    Sort.Order.desc("postId")
            )
    );

    private final Sort sort;

    PostSortType(Sort sort) {
        this.sort = sort;
    }

    public Pageable applyTo(Pageable pageable) {
        return PageRequest.of(
                pageable.getPageNumber(),
                pageable.getPageSize(),
                this.sort
        );
    }
}