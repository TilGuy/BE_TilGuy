package com.tilguys.matilda.tag.domain;

import com.tilguys.matilda.common.BaseEntity;
import com.tilguys.matilda.til.domain.Tag;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.ManyToOne;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SubTag extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotNull
    @Getter
    private String subTagContent;

    @Getter
    @ManyToOne
    private Tag tag;

    public SubTag(Tag tag, String subTagContent) {
        this.tag = tag;
        this.subTagContent = subTagContent;
    }
}
