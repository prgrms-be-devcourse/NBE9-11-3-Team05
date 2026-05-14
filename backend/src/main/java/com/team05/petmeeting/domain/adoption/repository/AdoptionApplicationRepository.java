package com.team05.petmeeting.domain.adoption.repository;

import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication;
import com.team05.petmeeting.domain.adoption.entity.AdoptionStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface AdoptionApplicationRepository extends JpaRepository<AdoptionApplication, Long> {
    List<AdoptionApplication> findByUser_Id(Long userId);
    Optional<AdoptionApplication> findByIdAndUser_Id(Long applicationId, Long userId);
    boolean existsByUser_IdAndAnimal_Id(Long userId, Long animalId);
    List<AdoptionApplication> findByAnimal_Shelter_CareRegNo(String careRegNo);
    List<AdoptionApplication> findByUser_IdAndStatus(Long userId, AdoptionStatus status);
}
