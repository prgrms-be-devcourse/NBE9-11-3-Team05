package com.team05.petmeeting.domain.user.repository;

import com.team05.petmeeting.domain.user.entity.User;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

@Repository
public interface UserRepository extends JpaRepository<User, Long> {

    Optional<User> findByEmail(String email);

    @Query("select distinct u from User u join fetch u.userAuths where u.email = :email")
    Optional<User> findByEmailWithAuths(String email);

    //User getById(Long id);
}