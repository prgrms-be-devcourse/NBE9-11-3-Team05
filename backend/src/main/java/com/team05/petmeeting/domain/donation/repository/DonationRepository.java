package com.team05.petmeeting.domain.donation.repository;

import com.team05.petmeeting.domain.donation.entity.Donation;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DonationRepository extends JpaRepository<Donation, Long> {
    List<Donation> findByUser_Id(Long userId);

    Donation findByPaymentId(String s);
}
