package net.rcetech.md.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.rcetech.md.domain.entity.MerchantSuccessStatus;

public interface MerchantSuccessStatusRepository extends JpaRepository<MerchantSuccessStatus, Long> {
}
