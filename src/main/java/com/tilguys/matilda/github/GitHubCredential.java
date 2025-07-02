package com.tilguys.matilda.github;

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
public class GitHubCredential {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Getter
    @JoinColumn(name = "user_id")
    @OneToOne(fetch = FetchType.LAZY)
    private TilUser tilUser;

    @Getter
    private String accessToken;

    @Getter
    private String repositoryName;

    @Getter
    private boolean isActivated;
}
