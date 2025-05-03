package com.tilguys.matilda.til.domain;

import com.tilguys.matilda.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.Table;
import java.time.LocalDate;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "til")
public class Til extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "til_id")
    private Long tilId;

    @Column(name = "user_id")
    private Long userId;

    @Column(name = "title")
    private String title;

    @Lob
    @Column(name = "content", columnDefinition = "MEDIUMTEXT")
    private String content;

    @Column(name = "date")
    private LocalDate date;

    @Column(name = "is_public")
    private boolean isPublic;

    @Column(name = "is_deleted")
    private boolean isDeleted;

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
}
