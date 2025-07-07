package com.tilguys.matilda.github;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tilguys.matilda.github.client.GitHubRepositoryClient;
import com.tilguys.matilda.github.domain.GitHubCommitPayload;
import com.tilguys.matilda.github.domain.GitHubRepository;
import com.tilguys.matilda.github.repository.GitHubCredentialRepository;
import com.tilguys.matilda.github.service.GitHubService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.time.LocalDate;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class GitHubServiceTest {

    @MockitoBean
    private GitHubRepositoryClient gitHubClient;

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private GitHubCredentialRepository gitHubCredentialRepository;

    private TilUser tilUser;

    @BeforeEach
    void setUp(
            @Autowired UserRepository userRepository,
            @Autowired TilRepository tilRepository
    ) {
        tilUser = createTilUser();
        userRepository.save(tilUser);
        tilRepository.save(createTil(tilUser));
    }

    @Test
    void 티일을_깃허브에_업로드한다() {
        // given
        Til til = createTil(tilUser);

        GitHubRepository gitHubCredential = createGitHubCredential(tilUser, true);
        gitHubCredentialRepository.save(gitHubCredential);

        doNothing().when(gitHubClient).uploadTilContent(any(GitHubCommitPayload.class));

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubService.uploadTilToGitHub(til));

        // verify
        verify(gitHubClient).uploadTilContent(any(GitHubCommitPayload.class));
    }

    @Test
    void 깃허브_크리덴셜이_비활성화된_경우_업로드하지_않는다() {
        // given
        gitHubCredentialRepository.deleteAll();

        Til til = createTil(tilUser);

        GitHubRepository inactiveCredential = createGitHubCredential(tilUser, false);
        gitHubCredentialRepository.save(inactiveCredential);

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubService.uploadTilToGitHub(til));

        // verify
        verify(gitHubClient, never()).uploadTilContent(any(GitHubCommitPayload.class));
    }

    @Test
    void 깃허브_크리덴셜_데이터가_없는_경우_업로드하지_않는다() {
        // given
        Til til = createTil(tilUser);
        gitHubCredentialRepository.deleteAll();

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubService.uploadTilToGitHub(til));

        // verify
        verify(gitHubClient, never()).uploadTilContent(any(GitHubCommitPayload.class));
    }

    private TilUser createTilUser() {
        return TilUser.builder()
                .providerInfo(ProviderInfo.GITHUB)
                .identifier("identifier")
                .role(Role.USER)
                .build();
    }

    private Til createTil(TilUser tilUser) {
        return Til.builder()
                .tilUser(tilUser)
                .title("테스트 TIL")
                .content("""
                        ## 테스트 내용
                        - 테스트 항목 1
                        - 테스트 항목 2
                        """)
                .date(LocalDate.of(2025, 6, 26))
                .build();
    }

    private GitHubRepository createGitHubCredential(TilUser tilUser, boolean isActivated) {
        return GitHubRepository.builder()
                .tilUser(tilUser)
                .accessToken("accessToken")
                .name("repositoryName")
                .isActivated(isActivated)
                .build();
    }
}
