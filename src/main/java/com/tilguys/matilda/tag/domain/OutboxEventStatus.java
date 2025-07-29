package com.tilguys.matilda.tag.domain;

public enum OutboxEventStatus {
    PENDING,    // 처리 대기 중
    PROCESSING, // 처리 중
    COMPLETED,  // 처리 완료
    FAILED      // 처리 실패
} 