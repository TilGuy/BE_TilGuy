package com.tilguys.matilda.security.info;

import com.tilguys.matilda.user.entity.ProviderInfo;
import java.util.Map;

public class GithubOAuth2UserInfo extends OAuth2UserInfo {
    public GithubOAuth2UserInfo(Map<String, Object> attributes) {
        super(attributes);
    }

    @Override
    public String getProviderCode() {
        return (String) attributes.get(ProviderInfo.GITHUB.getProvideCode());
    }

    @Override
    public String getUserIdentifier() {
        return (String) attributes.get(ProviderInfo.GITHUB.getIdentifier());
    }
}
