package tgb.cryptoexchange.merchantdetails.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.entity.Variable;
import tgb.cryptoexchange.merchantdetails.enums.ConfigType;

import java.util.Optional;

public interface VariableRepository extends JpaRepository<Variable, Long> {

    Optional<Variable> findByTypeAndConfigType(VariableType type, ConfigType configType);
}
