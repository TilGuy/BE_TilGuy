package com.tilguys.matilda.security.info;

import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public abstract class OAuth2UserInfo {

    protected final Map<String, Object> attributes;

    public abstract String getProviderCode();

    public abstract String getUserIdentifier();
}
