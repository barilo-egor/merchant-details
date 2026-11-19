package net.rcetech.md.domain.entity;

import jakarta.persistence.*;
import lombok.*;

/**
 * Соответствие client_id SmackPay -> наш id (OrderCreationRequest.userId).
 * Строки с client_id заводятся вручную с пустым user_id; при первом обращении
 * пользователя под него занимается свободная строка.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SmackPayClientId {

    @Id
    private String clientId;

    @Column(unique = true)
    private String userId;
}
