package com.team05.petmeeting.domain.adoption.dto

import com.team05.petmeeting.domain.adoption.entity.AdoptionApplication

fun AdoptionApplication.toApplyRes(): AdoptionApplyRes =
    AdoptionApplyRes(
        getId(),
        status,
        AdoptionApplyRes.AnimalInfo(
            animal.desertionNo,
            animal.kindFullNm,
            animal.careNm,
            animal.careOwnerNm,
        ),
    )

fun AdoptionApplication.toDetailRes(): AdoptionDetailRes =
    AdoptionDetailRes(
        getId(),
        status,
        applyReason,
        getCreatedAt(),
        reviewedAt,
        rejectionReason,
        applyTel,
        AdoptionDetailRes.AnimalInfo(
            animal.desertionNo,
            animal.specialMark,
            animal.careNm,
            animal.careOwnerNm,
            animal.careTel,
            animal.careAddr,
        ),
    )
