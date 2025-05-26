package com.tilguys.matilda.user;

public class TilUserFixture {

    private static final ProviderInfo DEFAULT_PROVIDER = ProviderInfo.GITHUB;
    private static final String DEFAULT_IDENTIFIER = "identifier";
    private static final Role DEFAULT_ROLE = Role.USER;
    private static final String DEFAULT_NICKNAME = "nickname";
    private static final String DEFAULT_AVATAR_URL = "avatarUrl";

    public static TilUser createTilUserFixture() {
        return TilUser.builder()
                .providerInfo(DEFAULT_PROVIDER)
                .identifier(DEFAULT_IDENTIFIER)
                .role(DEFAULT_ROLE)
                .nickname(DEFAULT_NICKNAME)
                .avatarUrl(DEFAULT_AVATAR_URL)
                .build();
    }
}
