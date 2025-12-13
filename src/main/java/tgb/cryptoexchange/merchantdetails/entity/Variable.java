package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;

/**
 * Динамическая переменная
 */
@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
@Builder
public class Variable {

    @Id
    @GeneratedValue
    private Long id;

    /**
     * Тип переменной
     */
    @Enumerated(EnumType.STRING)
    private VariableType type;

    /**
     * Значение
     */
    private String value;

    @Transient
    public Integer getInt() {
        return Integer.parseInt(value);
    }
}
