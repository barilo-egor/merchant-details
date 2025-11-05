package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.List;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    /**
     * Поля заполняемые в случае ошибки
     */
    private String code;

    private String message;

    private List<Error> errors;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.nonNull(code)) {
            result.expectedEmpty("code");
        }
        if (Objects.nonNull(message)) {
            result.expectedEmpty("message");
        }
        if (Objects.nonNull(errors)) {
            StringBuilder resultErrorsString = new StringBuilder();
            for (Error error : errors) {
                for (String field : error.getField()) {
                    resultErrorsString.append(field).append(", ");
                }
            }
            result.addError("errors", "expected empty but was: " + resultErrorsString);
        }
        if (hasDetails()) {
            validateDetails(result);
        }
        return result;
    }

    private void validateDetails(ValidationResult result) {
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.isNull(bankName)) {
            result.notNull("bankName");
        }
        if (Objects.isNull(status)) {
            result.notNull("status");
        }
        if (Objects.isNull(phoneNumber) && Objects.isNull(cardNumber)) {
            result.notNull("phoneNumber", "cardNumber");
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.isNull(code) && Objects.isNull(message) && (Objects.isNull(errors) || errors.isEmpty());
    }

    @Data
    public static class Error {

        private List<String> field;
    }

    @JsonProperty
    private Long id;

    @JsonProperty("bank_name")
    private String bankName;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("phone_number")
    private String phoneNumber;

    @JsonProperty("card_number")
    private String cardNumber;
}
