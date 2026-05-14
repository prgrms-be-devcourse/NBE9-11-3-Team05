package com.team05.petmeeting.domain.shelter.repository;

import com.team05.petmeeting.domain.shelter.entity.Shelter;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Set;

@Repository
public interface ShelterRepository extends JpaRepository<Shelter, String> {
    List<Shelter> findByCareRegNoIn(Set<String> ids);
}
