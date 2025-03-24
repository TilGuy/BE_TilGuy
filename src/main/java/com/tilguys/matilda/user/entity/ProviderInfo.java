package com.tilguys.matilda.user.entity;

import java.util.Arrays;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@RequiredArgsConstructor
@Getter
public enum ProviderInfo {

    GITHUB(null, "id", "login");

    private final String attributeKey;
    private final String provideCode;
    private final String identifier;

    public static ProviderInfo from(String provider) {
        String upperCastedProvider = provider.toUpperCase();
        return Arrays.stream(ProviderInfo.values())
                .filter(item -> item.name().equals(upperCastedProvider))
                .findFirst()
                .orElseThrow();
    }
}
