package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;
import tgb.cryptoexchange.merchantdetails.enums.ConfigType;

/**
 * Динамическая переменная
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
@Table(name = "variable", uniqueConstraints = {
        @UniqueConstraint(
                name = "uc_variable_config_type",
                columnNames = {"type", "config_type"}
        )
})
public class Variable {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Тип переменной
     */
    @Enumerated(EnumType.STRING)
    @Column(unique = true, nullable = false)
    private VariableType type;

    /**
     * Значение
     */
    private String value;

    @Transient
    public Integer getInt() {
        return Integer.parseInt(value);
    }

    @Enumerated(EnumType.STRING)
    public ConfigType configType;
}
