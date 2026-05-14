package com.team05.petmeeting.domain.animal.repository;

import com.team05.petmeeting.domain.animal.entity.Animal;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface AnimalRepositoryCustom {

    Page<Animal> findAnimalsWithFilter(
            String region,
            String kind,
            Integer stateGroup,
            Pageable pageable
    );


}
