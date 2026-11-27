package net.rcetech.md.domain.entity;

import jakarta.persistence.Column;
import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Соответствие client_id мерчанта -> наш id (OrderCreationRequest.userId).
 * Строки с client_id заводятся вручную с пустым user_id; при первом обращении
 * пользователя под него занимается свободная строка. Одна таблица на все
 * площадки с client_id, разбиение по merchant.
 */
@Entity
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MerchantClientId {

    @EmbeddedId
    private MerchantClientIdId id;

    @Column
    private String userId;
}
