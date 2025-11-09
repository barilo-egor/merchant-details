package tgb.cryptoexchange.merchantdetails.details.paybox;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

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
            result.expectedEmpty("code", code);
        }
        if (Objects.nonNull(message)) {
            result.expectedEmpty("message", message);
        }
        if (Objects.nonNull(errors) && !errors.isEmpty()) {
            String resultErrorsString = errors.stream()
                    .map(error -> String.join(", ", error.getField()))
                    .collect(Collectors.joining(", "));
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
        if (Objects.isNull(phoneNumber) && Objects.isNull(cardNumber)) {
            result.notNull("phoneNumber", "cardNumber");
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.isNull(code) && Objects.isNull(message)
                && (Objects.isNull(errors) || errors.isEmpty());
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
