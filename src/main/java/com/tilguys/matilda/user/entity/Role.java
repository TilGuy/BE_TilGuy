package com.tilguys.matilda.user.entity;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER("ROLE_USER", "일반 사용자");

    private final String key;
    private final String title;
}
