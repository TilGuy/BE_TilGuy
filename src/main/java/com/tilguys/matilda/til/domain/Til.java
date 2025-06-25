package com.tilguys.matilda.til.domain;

import com.tilguys.matilda.common.BaseEntity;
import com.tilguys.matilda.user.TilUser;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.Lob;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
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
    @Getter
    @Column(name = "til_id")
    private Long tilId;

    @Getter
    @JoinColumn(name = "user_id")
    @ManyToOne(fetch = FetchType.LAZY)
    private TilUser tilUser;

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

    @Getter
    @Column(name = "is_public")
    private boolean isPublic;

    @Getter
    @Column(name = "is_deleted")
    private boolean isDeleted;

    @Getter
    @OneToMany(mappedBy = "til", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Tag> tags = new ArrayList<>();

    @Getter
    @OneToMany(mappedBy = "til", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<Reference> references = new ArrayList<>();

    public void update(final String content, final boolean isPublic, final LocalDate date, String title) {
        this.content = content;
        this.isPublic = isPublic;
        this.date = date;
        this.title = title;
    }

    public void markAsDeletedBy(final Long userId) {
        if (!Objects.equals(this.tilUser.getId(), userId)) {
            throw new IllegalArgumentException("작성자만 삭제 가능합니다.");
        }
        this.isDeleted = true;
    }

    public boolean isWithinDateRange(final LocalDate from, final LocalDate to) {
        boolean isAfterOrEqualFrom = !date.isBefore(from);
        boolean isBeforeOrEqualTo = !date.isAfter(to);

        return isAfterOrEqualFrom && isBeforeOrEqualTo;
    }

    public void updateTags(List<Tag> newTags) {
        this.tags.clear();
        for (Tag tag : newTags) {
            tag.setTil(this);
            this.tags.add(tag);
        }
    }

    public void updateReferences(List<Reference> newReferences) {
        this.references.clear();
        for (Reference reference : newReferences) {
            reference.setTil(this);
            this.references.add(reference);
        }
    }

    public boolean isNotDeleted() {
        return !isDeleted;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) {
            return false;
        }
        final Til til = (Til) o;
        return Objects.equals(tilId, til.tilId);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(tilId);
    }
}
