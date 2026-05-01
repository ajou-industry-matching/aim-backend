package ajou.aim_be.post.repository;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.keyword.Keyword;
import ajou.aim_be.keyword.PostKeyword;
import ajou.aim_be.post.Post;
import jakarta.persistence.criteria.*;
import org.springframework.data.jpa.domain.Specification;

import java.util.ArrayList;
import java.util.List;

public class PostSpecification {

    public static Specification<Post> base(BoardType boardType) {
        return (root, query, cb) -> cb.and(
                cb.equal(root.get("boardType"), boardType),
                cb.equal(root.get("visibility"), Visibility.PUBLIC)
        );
    }

    public static Specification<Post> keywordOr(List<String> keywords) {
        return (root, query, cb) -> {

            query.distinct(true);

            Join<Post, PostKeyword> pk = root.join("postKeywords", JoinType.LEFT);
            Join<PostKeyword, Keyword> k = pk.join("keyword", JoinType.LEFT);

            List<Predicate> orPredicates = new ArrayList<>();

            for (String keyword : keywords) {
                String like = "%" + keyword.toLowerCase() + "%";

                orPredicates.add(cb.or(
                        cb.like(cb.lower(root.get("title")), like),
                        cb.like(cb.lower(root.get("description")), like),
                        cb.like(cb.lower(root.get("content")), like),
                        cb.like(cb.lower(k.get("keywordName")), like)
                ));
            }

            return cb.or(orPredicates.toArray(new Predicate[0]));
        };
    }
}