package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Data;

import java.time.Instant;

@Data
public class MerchantDetailsReceiveEvent {

    /**
     * Идентификатор сделки, по которому были запрошены реквизиты
     */
    private Long dealId;

    /**
     * Идентификатор пользователя, для которого были запрошены реквизиты
     */
    private Long userId;

    /**
     * Идентификатор приложения, запросившее реквизиты
     */
    private String initiatorApp;

    /**
     * Дата и время получения реквизитов
     */
    @JsonSerialize(using = InstantSerializer.class)
    private Instant createdAt;

    /**
     * Идентификатор мерчанта выдавшего реквизиты
     */
    private String merchant;

    /**
     * Идентификатор ордера в системе мерчанта
     */
    private String merchantOrderId;

    /**
     * Сумма, на которую были запрошены реквизиты
     */
    private Integer requestedAmount;

    /**
     * Обновленная мерчантом сумма
     */
    private Integer merchantAmount;

    /**
     * Тип реквизитов
     */
    private String method;

    /**
     * Реквизиты
     */
    private String details;
}
