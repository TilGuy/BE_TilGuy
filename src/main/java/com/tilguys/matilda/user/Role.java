package com.tilguys.matilda.user;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {

    USER;

    public static Role from(String findRole) {
        for (Role role : Role.values()) {
            if (role.toString().equals(findRole)) {
                return role;
            }
        }
        throw new IllegalArgumentException("존재하지 않는 권한입니다" + findRole);
    }
}
