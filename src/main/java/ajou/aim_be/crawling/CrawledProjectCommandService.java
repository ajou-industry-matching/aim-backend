package ajou.aim_be.crawling;

import ajou.aim_be.board.BoardType;
import ajou.aim_be.crawling.dto.CrawledProjectCreateRequest;
import ajou.aim_be.global.common.Visibility;
import ajou.aim_be.global.exception.CustomException;
import ajou.aim_be.global.exception.ErrorCode;
import ajou.aim_be.global.policy.PostPermissionPolicy;
import ajou.aim_be.global.policy.UserActionPolicy;
import ajou.aim_be.post.Post;
import ajou.aim_be.post.dto.PostDetailResponse;
import ajou.aim_be.post.repository.PostRepository;
import ajou.aim_be.post.service.PostResponseAssembler;
import ajou.aim_be.user.User;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class CrawledProjectCommandService {

    private final CrawledProjectRepository projectRepository;
    private final CrawledProjectMemberRepository memberRepository;
    private final PostRepository postRepository;
    private final PostResponseAssembler postResponseAssembler;

    @Transactional
    public void save(
            CrawledProjectCreateRequest request,
            User user
    ) {
        PostPermissionPolicy.validateCreatePermission(
                BoardType.CRAWLED_PROJECT,
                user
        );

        CrawledProject project =
                projectRepository
                        .findBySourceUidAndTerm(
                                request.getUid(),
                                request.getTerm()
                        )
                        .orElseGet(() ->
                                CrawledProject.builder()
                                        .sourceUid(request.getUid())
                                        .term(request.getTerm())
                                        .build()
                        );

        project.update(
                request.getTitle(),
                request.getSummary(),
                request.getDescription(),
                request.getContent(),
                request.getUrl(),
                request.getPresentationUrl(),
                request.getVideoUrl(),
                request.getGitRepository(),
                request.getRepresentativeImage(),
                request.getCategory()
        );

        project.clearMembers();

        if (request.getMembers() != null) {
            for (CrawledProjectCreateRequest.MemberRequest memberRequest
                    : request.getMembers()) {

                CrawledProjectMember member =
                        CrawledProjectMember.builder()
                                .project(project)
                                .role(memberRequest.getRole())
                                .name(memberRequest.getName())
                                .maskedEmail(memberRequest.getMaskedEmail())
                                .emailPrefix(
                                        extractEmailPrefix(
                                                memberRequest.getMaskedEmail()
                                        )
                                )
                                .department(memberRequest.getDepartment())
                                .grade(memberRequest.getGrade())
                                .build();

                project.addMember(member);
            }
        }

        projectRepository.save(project);
    }

    @Transactional
    public PostDetailResponse cloneToPortfolio(
            Long projectId,
            User user
    ) {
        UserActionPolicy.validateActive(user);

        CrawledProject project =
                projectRepository.findById(projectId)
                        .orElseThrow(() ->
                                new CustomException(
                                        ErrorCode.CRAWLED_PROJECT_NOT_FOUND
                                )
                        );

        boolean isMember =
                memberRepository.existsMyMembership(
                        projectId,
                        user.getName(),
                        user.getEmail()
                );

        if (!isMember) {
            throw new CustomException(
                    ErrorCode.CRAWLED_PROJECT_MEMBER_REQUIRED
            );
        }

        Post post = Post.builder()
                .user(user)
                .boardType(BoardType.PORTFOLIO)
                .title(project.getTitle())
                .description(project.getDescription())
                .content(project.getContent())
                .videoLink(project.getVideoUrl())
                .githubLink(project.getGithubUrl())
                .visibility(Visibility.PUBLIC)
                .build();

        postRepository.save(post);

        /*
         * 크롤링된 대표 이미지는 이미 외부 URL로 저장되어 있으므로
         * Firebase Storage에 새로 업로드하지 않고 URL만 복사한다.
         *
         * storageKey는 null로 유지한다.
         */
        if (project.getRepresentativeImage() != null &&
                !project.getRepresentativeImage().isBlank()) {

            post.updateThumbnail(
                    project.getRepresentativeImage(),
                    null
            );
        }

        return postResponseAssembler.assemble(
                post,
                user.getUserId()
        );
    }

    private String extractEmailPrefix(String maskedEmail) {

        if (maskedEmail == null || maskedEmail.isBlank()) {
            return null;
        }

        int at = maskedEmail.indexOf("@");

        if (at <= 0) {
            return null;
        }

        String local = maskedEmail.substring(0, at);

        int mask = local.indexOf("**");

        return mask >= 0
                ? local.substring(0, mask)
                : local;
    }
}