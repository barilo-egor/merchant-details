package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;
import java.util.Objects;

@Data
public class Response {

    /**
     * Поля заполняемые в случае ошибки
     */
    private String code;

    private String message;

    private List<Error> errors;

    @Data
    public static class Error {

        private List<String> field;
    }

    @JsonProperty
    private Long id;

    @JsonProperty("merchant_transaction_id")
    private String merchantTransactionId;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("card_number")
    private String cardNumber;

    public boolean hasErrors() {
        return (Objects.nonNull(code) && !code.isBlank())
                || (Objects.nonNull(message) && !message.isBlank())
                || (Objects.nonNull(errors) && !errors.isEmpty());
    }
}
