package com.tilguys.matilda.tag.domain;

import com.tilguys.matilda.common.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "tag_creation_outbox_events")
@Builder
@AllArgsConstructor
@NoArgsConstructor
@Getter
public class TagCreationOutboxEvent extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "til_id", nullable = false)
    private Long tilId;

    @Column(name = "til_content", columnDefinition = "TEXT", nullable = false)
    private String tilContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OutboxEventStatus status;

    @Column(name = "retry_count", nullable = false)
    @Builder.Default
    private Integer retryCount = 0;

    @Column(name = "error_message", columnDefinition = "TEXT")
    private String errorMessage;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(name = "scheduled_at", nullable = false)
    private LocalDateTime scheduledAt;

    // 상태 변경 메서드들
    public void markAsProcessing() {
        this.status = OutboxEventStatus.PROCESSING;
    }

    public void markAsCompleted() {
        this.status = OutboxEventStatus.COMPLETED;
        this.processedAt = LocalDateTime.now();
    }

    public void markAsFailed(String errorMessage) {
        this.status = OutboxEventStatus.FAILED;
        this.errorMessage = errorMessage;
        this.processedAt = LocalDateTime.now();
    }

    public void incrementRetryCount() {
        this.retryCount++;
    }

    public boolean canRetry() {
        return retryCount < 2 && status == OutboxEventStatus.FAILED;
    }

    public void reschedule(LocalDateTime newScheduledTime) {
        this.scheduledAt = newScheduledTime;
        this.status = OutboxEventStatus.PENDING;
    }
}
