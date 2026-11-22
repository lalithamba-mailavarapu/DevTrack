
package in.strikes.devTrack.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import in.strikes.devTrack.entity.PasswordResetToken;

public interface PasswordResetTokenRepository
        extends JpaRepository<PasswordResetToken, Long> {

    Optional<PasswordResetToken> findByTokenHash(String tokenHash);

    void deleteByUserId(Long userId);
}
