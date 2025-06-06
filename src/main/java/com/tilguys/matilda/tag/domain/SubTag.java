package com.tilguys.matilda.tag.domain;

import com.tilguys.matilda.common.BaseEntity;
import com.tilguys.matilda.til.domain.Tag;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class Tag extends BaseEntity {
public class SubTag {

    private final Tag tag;
    private final String subTag;

    public SubTag(Tag tag, String subTag) {
        this.tag = tag;
        this.subTag = subTag;
    }
}
