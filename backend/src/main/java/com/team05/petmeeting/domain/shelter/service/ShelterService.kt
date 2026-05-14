package com.team05.petmeeting.domain.shelter.service

import com.team05.petmeeting.domain.shelter.dto.ShelterCommand
import com.team05.petmeeting.domain.shelter.dto.ShelterRes
import com.team05.petmeeting.domain.shelter.entity.QShelter.shelter
import com.team05.petmeeting.domain.shelter.entity.Shelter
import com.team05.petmeeting.domain.shelter.entity.Shelter.Companion.create
import com.team05.petmeeting.domain.shelter.errorCode.ShelterErrorCode
import com.team05.petmeeting.domain.shelter.repository.ShelterRepository
import com.team05.petmeeting.global.exception.BusinessException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Collectors

@Service
@Transactional
class ShelterService(private val shelterRepository: ShelterRepository) {
    /*
        * 외부 API 데이터 예시
        * "careRegNo" : "343447202600001",
        * "careNm" : "음성군 동물보호센터",
        * "careTel" : "043-877-3081",
        * "careAddr" : "충청북도 음성군 삼성면 대금로 715-5",
        * "careOwnerNm" : "음성군수",
        * "orgNm" : "충청북도 음성군",
        * "updTm" : "2026-04-15 14:19:49.0"
        * updTm 비교해서 갱신 필요하면 보호소 정보도 갱신
        * cmd 1개 -> DB 조회 여러번
        */
    fun createOrUpdateShelter(cmd: ShelterCommand): Shelter {
        return shelterRepository.findById(cmd.careRegNo)
            .map(Function { existing: Shelter ->
                if (existing.updTm!!.isBefore(cmd.updTm)) {
                    existing.updateFrom(cmd)
                }
                existing
            })
            .orElseGet(Supplier {
                shelterRepository.save<Shelter>(
                    create(cmd)
                )
            })
    }

    /*
     * n개 cmd -> DB 조회 한번
     */
    fun createOrUpdateShelters(cmds: List<ShelterCommand>) {
        val ids = cmds.map {it.careRegNo} .toSet()

        // 모든 careRegNo 한번에 다 조회해서 map에 저장
        val map = shelterRepository.findByCareRegNoIn(ids)
            .stream()
            .collect(Collectors.toMap(Shelter::careRegNo, Function { s: Shelter? -> s }))

        for (cmd in cmds) {
            val existing = map[cmd.careRegNo] // map에 저장해둔 Shelter

            if (existing != null) {
                if (existing.updTm.isBefore(cmd.updTm)) {
                    existing.updateFrom(cmd)
                }
            } else {
                val newShelter = create(cmd)
                shelterRepository.save<Shelter>(newShelter)
                map[cmd.careRegNo] = newShelter
            }
        }
    }

    fun findById(id: String): Shelter {
        return shelterRepository.findById(id)
            .orElseThrow( { BusinessException(ShelterErrorCode.SHELTER_NOT_FOUND) })
    }

    fun getShelter(shelterId: String): ShelterRes {
        return shelterRepository.findById(shelterId)
            .map { shelter -> ShelterRes.from(shelter) }
            .orElseThrow { BusinessException(ShelterErrorCode.SHELTER_NOT_FOUND) }
    }
}
