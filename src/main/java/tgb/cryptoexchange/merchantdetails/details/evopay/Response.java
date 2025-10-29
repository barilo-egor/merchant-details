package tgb.cryptoexchange.merchantdetails.details.evopay;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private String id;

    private Status orderStatus;

    private Requisites requisites;

    // TODO заглушка. как на самом деле приходит метод пока неизвестно
    private Method method;

    @Data
    public static class Requisites {

        @JsonProperty("recipient_phone_number")
        private String recipientPhoneNumber;

        @JsonProperty("recipient_card_number")
        private String recipientCardNumber;

        @JsonProperty("recipient_bank")
        private String recipientBank;
    }
}
