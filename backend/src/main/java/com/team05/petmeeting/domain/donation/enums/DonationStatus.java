package com.team05.petmeeting.domain.donation.enums;

public enum DonationStatus {
    PENDING,
    PAID,
    FAILED,     // 결제 실패 or 금액 불일치
    CANCELED,   // 취소, 추후 구현
}
