package com.team05.petmeeting.domain.user.refreshtoken.repository;

import com.team05.petmeeting.domain.user.entity.User;
import com.team05.petmeeting.domain.user.refreshtoken.entity.RefreshToken;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    void deleteByToken(UUID token);

    Optional<RefreshToken> findByToken(UUID token);

    void deleteAllByUser(User user);

}
