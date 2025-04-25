package com.tilguys.matilda.user;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class TilUser {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProviderInfo providerInfo;

    @NotNull
    private String identifier;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    private String nickname;

    private String avatarUrl;

    public void updateAvatarUrl(String avatarUrl) {
        this.avatarUrl = avatarUrl;
    }
}
