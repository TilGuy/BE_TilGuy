package com.tilguys.matilda.github;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import com.tilguys.matilda.github.client.GitHubStorageClient;
import com.tilguys.matilda.github.domain.GitHubCommitPayload;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
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
    private GitHubStorageClient gitHubClient;

    @Autowired
    private GitHubService gitHubService;

    @Autowired
    private GitHubStorageRepository gitHubStorageRepository;

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
    void TIL을_깃허브에_업로드한다() {
        // given
        Til til = createTil(tilUser);

        GitHubStorage gitHubStorage = createGitHubStorage(tilUser, true);
        gitHubStorageRepository.save(gitHubStorage);

        doNothing().when(gitHubClient).uploadTilContent(any(GitHubCommitPayload.class));

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubService.uploadTilToGitHub(til));

        // verify
        verify(gitHubClient).uploadTilContent(any(GitHubCommitPayload.class));
    }

    @Test
    void 깃허브_저장소가_비활성화된_경우_업로드하지_않는다() {
        // given
        gitHubStorageRepository.deleteAll();

        Til til = createTil(tilUser);

        GitHubStorage inactiveStorage = createGitHubStorage(tilUser, false);
        gitHubStorageRepository.save(inactiveStorage);

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubService.uploadTilToGitHub(til));

        // verify
        verify(gitHubClient, never()).uploadTilContent(any(GitHubCommitPayload.class));
    }

    @Test
    void 깃허브_저장소_데이터가_없는_경우_업로드하지_않는다() {
        // given
        Til til = createTil(tilUser);
        gitHubStorageRepository.deleteAll();

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

    private GitHubStorage createGitHubStorage(TilUser tilUser, boolean isActivated) {
        return GitHubStorage.builder()
                .tilUser(tilUser)
                .accessToken("accessToken")
                .repositoryName("repositoryName")
                .isActivated(isActivated)
                .build();
    }
}
