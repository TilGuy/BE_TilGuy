package com.tilguys.matilda.github;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.github.controller.GitHubStorageRequest;
import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
import com.tilguys.matilda.github.service.GitHubStorageService;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class GitHubStorageServiceTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private GitHubStorageService gitHubStorageService;

    @Autowired
    private GitHubStorageRepository gitHubStorageRepository;

    private TilUser tilUser;

    @BeforeEach
    void setUp(
            @Autowired UserRepository userRepository
    ) {
        tilUser = createTilUser();
        userRepository.save(tilUser);
    }

    @Test
    void 깃허브_저장소_설정을_저장한다() {
        // given
        GitHubStorageRequest request = new GitHubStorageRequest("accessToken", "repositoryName");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(ResponseEntity.ok().build());

        // when
        gitHubStorageService.saveOrUpdateSettings(tilUser.getId(), request);
        Optional<GitHubStorage> savedStorage = gitHubStorageRepository.findByTilUserId(tilUser.getId());

        // then
        assertThat(savedStorage).isPresent();
        GitHubStorage result = savedStorage.get();

        assertAll(
                () -> assertThat(result.getAccessToken()).isEqualTo("accessToken"),
                () -> assertThat(result.getRepositoryName()).isEqualTo("repositoryName"),
                () -> assertThat(result.isActivated()).isTrue()
        );
    }

    @Test
    void 깃허브_저장소_설정을_업데이트한다() {
        // given
        GitHubStorage gitHubStorage = createGitHubStorage(tilUser);
        gitHubStorageRepository.save(gitHubStorage);

        GitHubStorageRequest request = new GitHubStorageRequest("newAccessToken", "newRepositoryName");
        when(restTemplate.exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        )).thenReturn(ResponseEntity.ok().build());

        // when
        gitHubStorageService.saveOrUpdateSettings(tilUser.getId(), request);
        Optional<GitHubStorage> updatedStorage = gitHubStorageRepository.findByTilUserId(tilUser.getId());

        // then
        assertThat(updatedStorage).isPresent();
        GitHubStorage result = updatedStorage.get();

        assertAll(
                () -> assertThat(result.getAccessToken()).isEqualTo("newAccessToken"),
                () -> assertThat(result.getRepositoryName()).isEqualTo("newRepositoryName"),
                () -> assertThat(result.isActivated()).isTrue()
        );
    }

    @Test
    void 잘못된_토큰으로_저장소_검증_실패시_예외를_던진다() {
        // given
        GitHubStorageRequest request = new GitHubStorageRequest("invalidToken", "repositoryName");
        doThrow(new RuntimeException()).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        );

        // when && then
        assertThatThrownBy(() -> gitHubStorageService.saveOrUpdateSettings(tilUser.getId(), request))
                .isInstanceOf(MatildaException.class)
                .hasMessage("GitHub 저장소가 존재하지 않거나 활성화되지 않았습니다. \nToken 및 저장소 이름을 확인해주세요.");
    }

    @Test
    void 잘못된_레포지토리_이름으로_저장소_검증_실패시_예외를_던진다() {
        // given
        GitHubStorageRequest request = new GitHubStorageRequest("accessToken", "invalidRepositoryName");
        doThrow(new RuntimeException()).when(restTemplate).exchange(
                anyString(),
                any(HttpMethod.class),
                any(HttpEntity.class),
                any(Class.class)
        );

        // when && then
        assertThatThrownBy(() -> gitHubStorageService.saveOrUpdateSettings(tilUser.getId(), request))
                .isInstanceOf(MatildaException.class)
                .hasMessage("GitHub 저장소가 존재하지 않거나 활성화되지 않았습니다. \nToken 및 저장소 이름을 확인해주세요.");
    }

    private TilUser createTilUser() {
        return TilUser.builder()
                .providerInfo(ProviderInfo.GITHUB)
                .identifier("identifier")
                .role(Role.USER)
                .build();
    }

    private GitHubStorage createGitHubStorage(TilUser tilUser) {
        return GitHubStorage.builder()
                .tilUser(tilUser)
                .accessToken("accessToken")
                .repositoryName("repositoryName")
                .isActivated(true)
                .build();
    }
}
