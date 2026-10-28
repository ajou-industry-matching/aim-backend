package ajou.aim_be.crawling;

import ajou.aim_be.crawling.dto.CrawledProjectMemberResponse;
import ajou.aim_be.crawling.dto.CrawledProjectResponse;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.global.policy.UserActionPolicy;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrawledProjectQueryService {

    private final CrawledProjectRepository projectRepository;
    private final CrawledProjectMemberRepository memberRepository;
    private final PostRepository postRepository;

    public List<CrawledProjectResponse> getMyProjects(User user) {

        UserActionPolicy.validateActive(user);

        List<CrawledProjectMember> matchedMembers =
                memberRepository.findMyProjects(
                        user.getName(),
                        user.getEmail()
                );

        if (matchedMembers.isEmpty()) {
            return List.of();
        }

        Set<Long> projectIds = matchedMembers.stream()
                .map(member -> member.getProject().getCrawledProjectId())
                .collect(Collectors.toCollection(LinkedHashSet::new));

        List<CrawledProject> projects =
                projectRepository.findDistinctByCrawledProjectIdIn(projectIds);


        List<Post> importedPosts =
                postRepository.findByUser_UserIdAndCrawledProjectIdIn(
                        user.getUserId(),
                        projectIds
                );

        Map<Long, Long> importedMap =
                importedPosts.stream()
                        .collect(Collectors.toMap(
                                Post::getCrawledProjectId,
                                Post::getPostId,
                                (existing, duplicate) -> existing
                        ));

        return projects.stream()
                .map(project -> {
                    Long portfolioPostId =
                            importedMap.get(project.getCrawledProjectId());

                    return CrawledProjectResponse.from(
                            project,
                            project.getMembers().stream()
                                    .map(CrawledProjectMemberResponse::from)
                                    .toList(),
                            portfolioPostId
                    );
                })
                .toList();
    }

    public CrawledProjectResponse getProject(
            Long projectId,
            User user
    ) {
        UserActionPolicy.validateActive(user);

        CrawledProject project =
                projectRepository.findWithMembersByCrawledProjectId(projectId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.CRAWLED_PROJECT_NOT_FOUND
                                )
                        );

        Long portfolioPostId =
                postRepository
                        .findFirstByUser_UserIdAndCrawledProjectIdOrderByPostIdAsc(
                                user.getUserId(),
                                projectId
                        )
                        .map(Post::getPostId)
                        .orElse(null);

        return CrawledProjectResponse.from(
                project,
                project.getMembers().stream()
                        .map(CrawledProjectMemberResponse::from)
                        .toList(),
                portfolioPostId
        );
    }
}