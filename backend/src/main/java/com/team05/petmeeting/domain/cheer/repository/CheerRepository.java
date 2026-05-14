package com.team05.petmeeting.domain.cheer.repository;

import com.team05.petmeeting.domain.cheer.entity.Cheer;
import com.team05.petmeeting.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface CheerRepository extends JpaRepository<Cheer, Long> {

    @Query("SELECT COUNT(DISTINCT c.animal) From Cheer c where c.user = :user")
    Long countDistinctAnimalByUser(User user);

    @Query("SELECT c.animal, COUNT(c) FROM Cheer c where c.user = :user GROUP BY c.animal")
    List<Object[]> findCheerCountsByUser(User user);
}