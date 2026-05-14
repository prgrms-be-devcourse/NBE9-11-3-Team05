package com.team05.petmeeting.domain.user.dto.profile;

import com.team05.petmeeting.domain.animal.entity.Animal;

import java.util.List;

public record UserCheerAnimalRes(
        long totalAnimalCount,
        List<CheerAnimalItem> animals
) {
    public static UserCheerAnimalRes from(List<Object[]> animalCountMap) {
        List<CheerAnimalItem> items = animalCountMap.stream()
                .map(row -> CheerAnimalItem.from((Animal) row[0], (Long) row[1]))
                .toList();

        return new UserCheerAnimalRes(items.size(), items);
    }

    public record CheerAnimalItem(
            Long animalId,
            String species,      // upKindNm (개/고양이)
            String breed,        // kindFullNm (믹스견 등)
            String imageUrl,     // popfile1
            long myCheerCount,   // "5개 보냄" (내가 이 동물에게 보낸 개수)
            double temperature   // "85.0C" (동물의 총 응원 수 기반 온도)
    ) {
        public static CheerAnimalItem from(Animal animal, long myCheerCount) {
            return new CheerAnimalItem(
                    animal.getId(),
                    animal.getUpKindNm(),
                    animal.getKindFullNm(),
                    animal.getPopfile1(),
                    myCheerCount,
                    animal.getTemperature()
            );
        }
    }

}
