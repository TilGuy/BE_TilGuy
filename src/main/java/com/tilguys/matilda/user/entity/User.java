package com.tilguys.matilda.user.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@Builder
@AllArgsConstructor
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
}
