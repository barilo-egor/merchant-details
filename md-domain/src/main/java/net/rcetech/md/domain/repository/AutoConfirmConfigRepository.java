package net.rcetech.md.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.rcetech.md.domain.entity.AutoConfirmConfig;

public interface AutoConfirmConfigRepository extends JpaRepository<AutoConfirmConfig, Long> {
}
