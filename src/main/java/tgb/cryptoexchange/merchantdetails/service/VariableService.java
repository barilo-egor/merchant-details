package tgb.cryptoexchange.merchantdetails.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.entity.Variable;
import tgb.cryptoexchange.merchantdetails.enums.ConfigType;
import tgb.cryptoexchange.merchantdetails.repository.VariableRepository;

import java.util.Optional;

@Service
public class VariableService {

    private final VariableRepository variableRepository;

    public VariableService(VariableRepository variableRepository) {
        this.variableRepository = variableRepository;
    }


    @PostConstruct
    public void init() {
        for (VariableType variableType : VariableType.values()) {
            updateConfigTypeIfNull(variableType);
            findByTypeAndConfigType(variableType, ConfigType.BOT);
            findByTypeAndConfigType(variableType, ConfigType.API);
        }
    }

    public Variable findByTypeAndConfigType(VariableType type, ConfigType configType) {
        return variableRepository.findByTypeAndConfigType(type, configType)
                .orElseGet(() -> {
                    Variable newVariable = new Variable();
                    newVariable.setType(type);
                    newVariable.setValue(type.getDefaultValue(configType));
                    newVariable.setConfigType(configType);
                    return variableRepository.save(newVariable);
                });
    }

    public void update(VariableType variableType, ConfigType configType, String value) {
        Variable variable = findByTypeAndConfigType(variableType, configType);
        variable.setValue(value);
        variableRepository.save(variable);
    }

    public void updateConfigTypeIfNull(VariableType variableType) {
        Optional<Variable> variable = variableRepository.findByTypeAndConfigType(variableType, null);
        variable.ifPresent(x -> {
            x.setConfigType(ConfigType.BOT);
            variableRepository.save(x);
        });
    }
}
