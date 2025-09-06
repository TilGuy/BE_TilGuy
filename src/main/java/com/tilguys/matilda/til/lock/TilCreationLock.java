package com.tilguys.matilda.til.lock;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import java.time.LocalDate;
import java.time.LocalDateTime;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "til_creation_lock", 
       uniqueConstraints = @UniqueConstraint(
           name = "uk_til_creation_lock_user_date",
           columnNames = {"user_id", "lock_date"}
       ))
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class TilCreationLock {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "user_id", nullable = false)
    private Long userId;

    @Column(name = "lock_date", nullable = false)
    private LocalDate lockDate;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "expires_at", nullable = false)
    private LocalDateTime expiresAt;

    public static TilCreationLock create(Long userId, LocalDate lockDate) {
        LocalDateTime now = LocalDateTime.now();
        return TilCreationLock.builder()
                .userId(userId)
                .lockDate(lockDate)
                .createdAt(now)
                .expiresAt(now.plusMinutes(5))
                .build();
    }

    public boolean isExpired() {
        return LocalDateTime.now().isAfter(expiresAt);
    }
}
