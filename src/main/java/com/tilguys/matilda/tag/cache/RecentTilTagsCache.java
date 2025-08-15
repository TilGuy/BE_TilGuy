package com.tilguys.matilda.tag.cache;

import com.tilguys.matilda.common.auth.exception.MatildaException;
import com.tilguys.matilda.common.cache.RedisCacheService;
import com.tilguys.matilda.tag.domain.TilTagRelations;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.logging.Logger;

@Component
public class RecentTilTagsCache {

    private static final String RECENT_TAG_RELATIONS_KEY = "recent:til:relations";
    // 스케줄 주기(30m)보다 약간 긴 TTL로 설정
    private static final Duration RECENT_TAG_RELATIONS_TTL = Duration.ofMinutes(40);
    
    private static final Logger logger = Logger.getLogger(RecentTilTagsCache.class.getName());

    private final RedisCacheService redisCacheService;

    public RecentTilTagsCache(RedisCacheService redisCacheService) {
        this.redisCacheService = redisCacheService;
    }

    public TilTagRelations getRecentTagRelations() {
        return redisCacheService.get(RECENT_TAG_RELATIONS_KEY, TilTagRelations.class)
                .orElseThrow(() -> new MatildaException("캐시를 가져오는데 실패하였습니다"));
    }

    public void updateRecentTagRelations(TilTagRelations recentTagRelations) {
        // 안전한 메모리 분석 수행
        analyzeMemoryUsage(recentTagRelations);
        
        redisCacheService.set(RECENT_TAG_RELATIONS_KEY, recentTagRelations, RECENT_TAG_RELATIONS_TTL);
    }
    
    /**
     * 안전한 메모리 사용량 분석을 수행합니다.
     * JOL 대신 Java 내장 기능을 사용하여 클래스 로더 이슈를 방지합니다.
     */
    private void analyzeMemoryUsage(TilTagRelations recentTagRelations) {
        try {
            // 객체 크기 추정 (정확하지 않지만 안전함)
            long estimatedSize = estimateObjectSize(recentTagRelations);
            
            // 로깅 (개발 환경에서만 상세 로그 출력)
            if (logger.isLoggable(java.util.logging.Level.FINE)) {
                logger.fine("=== TilTagRelations Memory Usage Analysis ===");
                logger.fine("Estimated Memory Usage: " + estimatedSize + " bytes");
                logger.fine("Object Class: " + recentTagRelations.getClass().getName());
                logger.fine("Class Loader: " + recentTagRelations.getClass().getClassLoader());
            }
            
            // 메모리 사용량이 임계값을 초과하는 경우 경고 로그
            if (estimatedSize > 1024 * 1024) { // 1MB 이상
                logger.warning("TilTagRelations estimated memory usage exceeds 1MB: " + estimatedSize + " bytes");
            }
            
            // 객체 상태 정보 로깅
            logObjectState(recentTagRelations);
            
        } catch (Exception e) {
            logger.warning("Memory usage analysis failed: " + e.getMessage());
        }
    }
    
    /**
     * 객체의 크기를 추정합니다.
     * 정확하지 않지만 안전하고 안정적인 방법입니다.
     */
    private long estimateObjectSize(TilTagRelations object) {
        if (object == null) return 0;
        
        long size = 0;
        
        try {
            // 기본 객체 헤더 (12-16 바이트)
            size += 16;
            
            // 객체의 필드들을 반영한 크기 추정
            // 실제 구현에서는 더 정확한 계산이 가능합니다
            size += estimateFieldsSize(object);
            
        } catch (Exception e) {
            logger.fine("Detailed size estimation failed, using basic estimation: " + e.getMessage());
            // 기본 추정치 사용
            size = 1024; // 1KB 기본값
        }
        
        return size;
    }
    
    /**
     * 객체의 필드 크기를 추정합니다.
     */
    private long estimateFieldsSize(TilTagRelations object) {
        long fieldsSize = 0;
        
        try {
            // Reflection을 사용하여 필드 정보 분석
            java.lang.reflect.Field[] fields = object.getClass().getDeclaredFields();
            
            for (java.lang.reflect.Field field : fields) {
                field.setAccessible(true);
                Object value = field.get(object);
                
                if (value != null) {
                    fieldsSize += estimateFieldValueSize(value);
                }
            }
            
        } catch (Exception e) {
            logger.fine("Field size estimation failed: " + e.getMessage());
        }
        
        return fieldsSize;
    }
    
    /**
     * 필드 값의 크기를 추정합니다.
     */
    private long estimateFieldValueSize(Object value) {
        if (value == null) return 0;
        
        if (value instanceof String) {
            return ((String) value).length() * 2 + 24; // String 객체 크기
        } else if (value instanceof java.util.Collection) {
            return 24 + ((java.util.Collection<?>) value).size() * 8; // Collection 기본 크기
        } else if (value instanceof java.util.Map) {
            return 24 + ((java.util.Map<?, ?>) value).size() * 16; // Map 기본 크기
        } else if (value.getClass().isArray()) {
            return 24 + java.lang.reflect.Array.getLength(value) * 8; // Array 기본 크기
        } else {
            return 8; // 기본 객체 참조 크기
        }
    }
    
    /**
     * 객체의 상태 정보를 로깅합니다.
     */
    private void logObjectState(TilTagRelations object) {
        try {
            if (logger.isLoggable(java.util.logging.Level.FINE)) {
                logger.fine("Object State Analysis:");
                logger.fine("- Hash Code: " + System.identityHashCode(object));
                logger.fine("- Class: " + object.getClass().getName());
                logger.fine("- Class Loader: " + object.getClass().getClassLoader());
                logger.fine("- Superclass: " + object.getClass().getSuperclass());
                logger.fine("- Interfaces: " + java.util.Arrays.toString(object.getClass().getInterfaces()));
            }
        } catch (Exception e) {
            logger.fine("Object state logging failed: " + e.getMessage());
        }
    }
    
    /**
     * 객체의 메모리 사용량을 추정하여 반환합니다.
     * 모니터링 및 성능 분석에 활용할 수 있습니다.
     */
    public long getEstimatedMemorySize(TilTagRelations object) {
        try {
            return estimateObjectSize(object);
        } catch (Exception e) {
            logger.warning("Failed to estimate memory size: " + e.getMessage());
            return -1;
        }
    }
    
    /**
     * 객체의 상세한 분석 정보를 문자열로 반환합니다.
     * 개발자 도구나 모니터링 시스템에서 활용할 수 있습니다.
     */
    public String getObjectAnalysisInfo(TilTagRelations object) {
        try {
            StringBuilder sb = new StringBuilder();
            sb.append("=== Object Analysis Info ===\n");
            sb.append("Class: ").append(object.getClass().getName()).append("\n");
            sb.append("Class Loader: ").append(object.getClass().getClassLoader()).append("\n");
            sb.append("Hash Code: ").append(System.identityHashCode(object)).append("\n");
            sb.append("Estimated Size: ").append(estimateObjectSize(object)).append(" bytes\n");
            sb.append("Superclass: ").append(object.getClass().getSuperclass()).append("\n");
            sb.append("Interfaces: ").append(java.util.Arrays.toString(object.getClass().getInterfaces())).append("\n");
            return sb.toString();
        } catch (Exception e) {
            return "Object analysis failed: " + e.getMessage();
        }
    }
}
