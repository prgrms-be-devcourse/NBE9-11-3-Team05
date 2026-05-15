package com.team05.petmeeting.domain.animal.config

import org.springframework.boot.context.properties.ConfigurationProperties

@ConfigurationProperties(prefix = "animal.sync")
class AnimalSyncProperties {
    // 최초 적재 기본 설정
    var initial: Initial = Initial()
    // 업데이트 적재 기본 설정
    var update: Update = Update()

    class Initial {
        // 최초 적재 시 기본 조회 건수
        var numOfRows: Int = 500
    }

    class Update {
        // 업데이트 적재 시 기본 조회 건수
        var numOfRows: Int = 500
        // 업데이트 페이지 호출 간 대기 시간
        var delayMs: Long = 300L
    }
}
