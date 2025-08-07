package com.tilguys.matilda.tag.repository;

import com.tilguys.matilda.tag.domain.OutboxEventStatus;
import com.tilguys.matilda.tag.domain.TagCreationOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface TagCreationOutboxEventRepository extends JpaRepository<TagCreationOutboxEvent, Long> {

    /**
     * 처리 대기중인 이벤트들을 스케줄 시간 순으로 조회
     */
    @Query("SELECT e FROM TagCreationOutboxEvent e " +
            "WHERE e.status = 'PENDING' " +
            "AND e.scheduledAt <= :now " +
            "ORDER BY e.scheduledAt ASC")
    List<TagCreationOutboxEvent> findPendingEvents(@Param("now") LocalDateTime now);

    /**
     * 재시도 가능한 실패 이벤트들 조회
     */
    @Query("SELECT e FROM TagCreationOutboxEvent e " +
            "WHERE e.status = 'FAILED' " +
            "AND e.retryCount < 2 " +
            "AND e.scheduledAt <= :now " +
            "ORDER BY e.scheduledAt ASC")
    List<TagCreationOutboxEvent> findRetryableFailedEvents(@Param("now") LocalDateTime now);

    /**
     * 특정 TIL의 처리 상태 확인
     */
    List<TagCreationOutboxEvent> findByTilIdAndStatus(Long tilId, OutboxEventStatus status);

    /**
     * 오래된 완료/실패 이벤트 정리용
     */
    @Query("SELECT e FROM TagCreationOutboxEvent e " +
            "WHERE e.status IN ('COMPLETED', 'FAILED') " +
            "AND e.processedAt < :cutoffDate")
    List<TagCreationOutboxEvent> findOldProcessedEvents(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * 특정 기간 내 이벤트 통계
     */
    @Query("SELECT e.status, COUNT(e) FROM TagCreationOutboxEvent e " +
            "WHERE e.createdAt >= :fromDate " +
            "GROUP BY e.status")
    List<Object[]> getEventStatistics(@Param("fromDate") LocalDateTime fromDate);
}
