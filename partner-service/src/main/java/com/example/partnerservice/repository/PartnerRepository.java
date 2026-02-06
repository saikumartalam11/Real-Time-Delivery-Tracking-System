package com.example.partnerservice.repository;

import com.example.partnerservice.model.Partner;
import org.springframework.data.jpa.repository.JpaRepository;

public interface PartnerRepository extends JpaRepository<Partner, Long> {}
