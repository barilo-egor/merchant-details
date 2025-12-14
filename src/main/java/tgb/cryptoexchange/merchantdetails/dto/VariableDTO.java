package tgb.cryptoexchange.merchantdetails.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.entity.Variable;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class VariableDTO {

    private VariableType type;

    private String value;

    public static VariableDTO fromEntity(Variable variable) {
        return VariableDTO.builder()
                .type(variable.getType())
                .value(variable.getValue())
                .build();
    }
}
