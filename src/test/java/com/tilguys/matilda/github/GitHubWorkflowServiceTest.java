package com.tilguys.matilda.github;

import static org.assertj.core.api.Assertions.assertThatNoException;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.tilguys.matilda.github.domain.GitHubStorage;
import com.tilguys.matilda.github.repository.GitHubStorageRepository;
import com.tilguys.matilda.github.service.GitHubWorkflowService;
import com.tilguys.matilda.til.domain.Til;
import com.tilguys.matilda.til.repository.TilRepository;
import com.tilguys.matilda.user.ProviderInfo;
import com.tilguys.matilda.user.Role;
import com.tilguys.matilda.user.TilUser;
import com.tilguys.matilda.user.repository.UserRepository;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestTemplate;

@Transactional
@SpringBootTest
@ActiveProfiles("test")
class GitHubWorkflowServiceTest {

    @MockitoBean
    private RestTemplate restTemplate;

    @Autowired
    private GitHubWorkflowService gitHubWorkflowService;

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

        doNothing().when(restTemplate).put(
                any(String.class),
                any(HttpEntity.class)
        );

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubWorkflowService.uploadTilToGitHub(til));

        // verify
        verify(restTemplate).put(any(String.class), any(HttpEntity.class));
    }

    @Test
    void 깃허브_저장소_업로드가_비활성화된_경우_업로드하지_않는다() {
        // given
        gitHubStorageRepository.deleteAll();

        Til til = createTil(tilUser);

        GitHubStorage inactiveStorage = createGitHubStorage(tilUser, false);
        gitHubStorageRepository.save(inactiveStorage);

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubWorkflowService.uploadTilToGitHub(til));

        // verify
        verify(restTemplate, never()).put(any(String.class), any(HttpEntity.class));
    }

    @Test
    void 깃허브_저장소_데이터가_없는_경우_업로드하지_않는다() {
        // given
        Til til = createTil(tilUser);
        gitHubStorageRepository.deleteAll();

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubWorkflowService.uploadTilToGitHub(til));

        // verify
        verify(restTemplate, never()).put(any(String.class), any(HttpEntity.class));
    }

    @Test
    void TIL을_깃허브에서_수정한다() {
        // given
        Til til = createTil(tilUser);
        GitHubStorage gitHubStorage = createGitHubStorage(tilUser, true);
        gitHubStorageRepository.save(gitHubStorage);

        Map<String, Object> contentResponse = new HashMap<>();
        contentResponse.put("sha", "test_sha");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(contentResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        doNothing().when(restTemplate).put(
                any(String.class),
                any(HttpEntity.class)
        );

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubWorkflowService.updateTilToGitHub(til));

        // verify
        verify(restTemplate).exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).put(any(String.class), any(HttpEntity.class));
    }

    @Test
    void TIL을_깃허브에서_삭제한다() {
        // given
        Til til = createTil(tilUser);
        GitHubStorage gitHubStorage = createGitHubStorage(tilUser, true);
        gitHubStorageRepository.save(gitHubStorage);

        Map<String, Object> contentResponse = new HashMap<>();
        contentResponse.put("sha", "test_sha");
        ResponseEntity<Map<String, Object>> responseEntity = new ResponseEntity<>(contentResponse, HttpStatus.OK);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.GET),
                any(HttpEntity.class),
                any(ParameterizedTypeReference.class)
        )).thenReturn(responseEntity);

        when(restTemplate.exchange(
                any(String.class),
                eq(HttpMethod.DELETE),
                any(HttpEntity.class),
                eq(Void.class)
        )).thenReturn(new ResponseEntity<>(HttpStatus.OK));

        // when && then
        assertThatNoException()
                .isThrownBy(() -> gitHubWorkflowService.deleteTilToGitHub(til));

        // verify
        verify(restTemplate).exchange(any(String.class), eq(HttpMethod.GET), any(HttpEntity.class), any(ParameterizedTypeReference.class));
        verify(restTemplate).exchange(any(String.class), eq(HttpMethod.DELETE), any(HttpEntity.class), eq(Void.class));
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
