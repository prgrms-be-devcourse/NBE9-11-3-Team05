package com.team05.petmeeting.domain.comment.repository;

import com.team05.petmeeting.domain.comment.entity.AnimalComment;
import com.team05.petmeeting.domain.user.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnimalCommentRepository extends JpaRepository<AnimalComment, Long> {
    List<AnimalComment> findByAnimal_Id(Long animalId);

    List<AnimalComment> findAllByUserOrderByCreatedAtDesc(User user);

    Long countAnimalCommentByUser(User user);
}
