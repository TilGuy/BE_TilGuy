package com.tilguys.matilda.til.domain;

import com.tilguys.matilda.common.BaseEntity;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "til")
public class Til extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "til_id")
    @Getter
    private Long tilId;

    @Column(name = "user_id")
    private Long userId;

    @Getter
    @Column(name = "title")
    private String title;

    @Getter
    @Lob
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @Getter
    @Column(name = "date")
    private LocalDate date;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Getter
    @OneToMany(mappedBy = "til", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    public void updateContentAndVisibility(final String content, final boolean isPublic) {
        this.content = content;
        this.isPublic = isPublic;
    }

    public void markAsDeleted() {
        this.isDeleted = true;
    }

    public boolean isWithinDateRange(final LocalDate from, final LocalDate to) {
        boolean isAfterOrEqualFrom = !date.isBefore(from);
        boolean isBeforeOrEqualTo = !date.isAfter(to);

        return isAfterOrEqualFrom && isBeforeOrEqualTo;
    }

    //til
    public void updateTags(List<Tag> newTags) {
        this.tags.clear(); // 기존 태그 삭제
        for (Tag tag : newTags) {
            tag.setTil(this); // 양방향 연관관계 설정
            this.tags.add(tag);
        }
    }
}
