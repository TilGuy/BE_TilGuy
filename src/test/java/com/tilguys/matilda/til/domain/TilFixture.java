package com.tilguys.matilda.til.domain;

import com.tilguys.matilda.user.TilUser;
import java.time.LocalDate;
import java.util.ArrayList;

public class TilFixture {

    private static final String DEFAULT_TITLE = "title";
    private static final String DEFAULT_CONTENT = "content";
    private static final LocalDate DEFAULT_DATE = LocalDate.now();

    public static Til createTilFixture(TilUser tilUser, boolean isPublic, boolean isDeleted) {
        return Til.builder()
                .tilUser(tilUser)
                .title(DEFAULT_TITLE)
                .content(DEFAULT_CONTENT)
                .date(DEFAULT_DATE)
                .isPublic(isPublic)
                .isDeleted(isDeleted)
                .tags(new ArrayList<>())
                .build();
    }

    public static Til createTilFixture(TilUser tilUser) {
        return Til.builder()
                .tilUser(tilUser)
                .title(DEFAULT_TITLE)
                .content(DEFAULT_CONTENT)
                .date(DEFAULT_DATE)
                .isPublic(true)
                .isDeleted(false)
                .tags(new ArrayList<>())
                .build();
    }
}
