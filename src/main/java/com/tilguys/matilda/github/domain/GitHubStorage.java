package com.tilguys.matilda.github.domain;

import com.tilguys.matilda.user.TilUser;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class GitHubStorage {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private TilUser tilUser;

    @Getter
    private String repositoryName;

    @Getter
    private String accessToken;

    @Getter
    private boolean isActivated;

    public void updateAccessTokenAndRepositoryName(String accessToken, String repositoryName) {
        this.accessToken = accessToken;
        this.repositoryName = repositoryName;
    }
}
