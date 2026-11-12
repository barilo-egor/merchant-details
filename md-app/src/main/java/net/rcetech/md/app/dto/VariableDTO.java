package net.rcetech.md.app.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.domain.entity.Variable;

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
