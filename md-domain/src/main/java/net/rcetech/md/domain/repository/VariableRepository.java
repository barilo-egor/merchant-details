package net.rcetech.md.domain.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.domain.entity.Variable;
import net.rcetech.md.meta.enums.ConfigType;

import java.util.Optional;

public interface VariableRepository extends JpaRepository<Variable, Long> {

    Optional<Variable> findByTypeAndConfigType(VariableType type, ConfigType configType);
}
