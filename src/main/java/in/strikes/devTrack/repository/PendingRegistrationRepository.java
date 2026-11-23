
package in.strikes.devTrack.repository;

import java.time.LocalDateTime;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.strikes.devTrack.entity.PendingRegistration;

public interface PendingRegistrationRepository
        extends JpaRepository<PendingRegistration, Long> {

    Optional<PendingRegistration> findByTokenHash(String tokenHash);

    boolean existsByEmail(String email);

    boolean existsByUsername(String username);

    void deleteByEmail(String email);

    void deleteByUsername(String username);

    void deleteByExpiresAtBefore(LocalDateTime time);
}
