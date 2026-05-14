package com.team05.petmeeting.domain.naming.repository;

import com.team05.petmeeting.domain.naming.entity.BadWord;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BadWordRepository extends JpaRepository<BadWord, Long> {
}
