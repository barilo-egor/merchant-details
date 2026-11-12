package net.rcetech.md.app.service;

import jakarta.annotation.PostConstruct;
import org.springframework.stereotype.Service;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.domain.entity.Variable;
import net.rcetech.md.meta.enums.ConfigType;
import net.rcetech.md.domain.repository.VariableRepository;

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
        }
    }

    public Variable findByTypeAndConfigType(VariableType type, ConfigType configType) {
        return variableRepository.findByTypeAndConfigType(type, configType)
                .orElseGet(() -> {
                    Variable newVariable = new Variable();
                    newVariable.setType(type);
                    newVariable.setValue(type.getDefaultValue());
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
