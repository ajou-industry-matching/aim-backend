package ajou.aim_be.crawling;

import ajou.aim_be.crawling.dto.CrawledProjectMemberResponse;
import ajou.aim_be.crawling.dto.CrawledProjectResponse;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.global.policy.UserActionPolicy;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CrawledProjectQueryService {

    private final CrawledProjectRepository projectRepository;
    private final CrawledProjectMemberRepository memberRepository;

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

        return projects.stream()
                .map(project -> CrawledProjectResponse.from(
                        project,
                        project.getMembers().stream()
                                .map(CrawledProjectMemberResponse::from)
                                .toList()
                ))
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

        return CrawledProjectResponse.from(
                project,
                project.getMembers().stream()
                        .map(CrawledProjectMemberResponse::from)
                        .toList()
        );
    }
}