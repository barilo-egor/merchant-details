package net.rcetech.md.domain.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import net.rcetech.md.meta.constants.VariableType;
import net.rcetech.md.meta.enums.ConfigType;

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
                name = "uk_variable_type_config_type",
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
    @Column(nullable = false)
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
