package com.aigym.repository;

import com.aigym.domain.entity.RefreshTokenSession;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

@Repository
public interface RefreshTokenSessionRepository extends JpaRepository<RefreshTokenSession, Long> {
    Optional<RefreshTokenSession> findByJti(String jti);

    List<RefreshTokenSession> findByUserIdAndRevokedAtIsNull(Long userId);

    List<RefreshTokenSession> findByExpiresAtBeforeAndRevokedAtIsNull(Instant now);
}
