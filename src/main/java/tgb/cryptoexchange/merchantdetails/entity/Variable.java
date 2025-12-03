package tgb.cryptoexchange.merchantdetails.entity;

import jakarta.persistence.*;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.constants.VariableType;

/**
 * Динамическая переменная
 */
@Entity
@Data
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
