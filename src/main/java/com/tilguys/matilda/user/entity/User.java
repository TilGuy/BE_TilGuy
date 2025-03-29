package com.tilguys.matilda.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

    @NotNull
    @Enumerated(EnumType.STRING)
    private ProviderInfo providerInfo;

    @NotNull
    @Id
    private String identifier;

    @NotNull
    @Enumerated(EnumType.STRING)
    private Role role;

    private String nickname;

    @Builder
    public User(ProviderInfo providerInfo, String identifier, Role role, String nickname) {
        this.providerInfo = providerInfo;
        this.identifier = identifier;
        this.role = role;
        this.nickname = nickname;
    }
}
