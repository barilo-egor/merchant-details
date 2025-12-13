package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.stereotype.Service;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.entity.Variable;
import tgb.cryptoexchange.merchantdetails.repository.VariableRepository;

@Service
public class VariableService {

    private final VariableRepository variableRepository;

    public VariableService(VariableRepository variableRepository) {
        this.variableRepository = variableRepository;
    }

    public Variable findByType(VariableType type) {
        return variableRepository.findByType(type)
                .orElseGet(() -> {
                    Variable newVariable = new Variable();
                    newVariable.setType(type);
                    newVariable.setValue(type.getDefaultValue());
                    return variableRepository.save(newVariable);
                });
    }
}
