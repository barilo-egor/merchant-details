package net.rcetech.md.app.kafka;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.Data;

import java.time.Instant;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MerchantDetailsReceiveEvent {

    /**
     * Идентификатор операции, по которой были получены реквизиты
     */
    private String operationId;

    /**
     * Идентификатор получаетеля реквизитов
     */
    private String actorId;

    /**
     * Идентификатор приложения, запросившее реквизиты
     */
    private String initiatorApp;

    /**
     * Дата и время получения реквизитов
     */
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

    /**
     * ссылка на qr/платежку
     */
    private String paymentLink;

}
