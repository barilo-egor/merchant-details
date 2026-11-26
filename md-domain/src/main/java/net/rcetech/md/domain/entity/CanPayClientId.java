package net.rcetech.md.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Соответствие client_id CanPay -> наш id (OrderCreationRequest.userId).
 * Строки с client_id заводятся вручную с пустым user_id; при первом обращении
 * пользователя под него занимается свободная строка.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CanPayClientId {

    @Id
    private String clientId;

    @Column(unique = true)
    private String userId;
}
