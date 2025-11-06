package tgb.cryptoexchange.merchantdetails.details.nicepay;

import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.io.IOException;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    @JsonDeserialize(using = ResponseStatus.Deserializer.class)
    private ResponseStatus status;

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(status)) {
            result.notNull("status");
        } else {
            if (ResponseStatus.ERROR.equals(status)) {
                result.addError("status", "must not be " + ResponseStatus.ERROR.name());
            } else if (ResponseStatus.DETAILS_FOUND.equals(status)) {
                validateData(result);
            }
        }
        return result;
    }

    private void validateData(ValidationResult result) {
        if (Objects.isNull(data)) {
            result.notNull("data");
        } else {
            if (Objects.isNull(data.getPaymentId())) {
                result.notNull("data.paymentId");
            }
            if (Objects.isNull(data.getDetails())) {
                result.notNull("data.details");
            } else {
                if (Objects.isNull(data.getDetails().getWallet())) {
                    result.notNull("data.details.wallet");
                }
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return ResponseStatus.DETAILS_FOUND.equals(status);
    }

    @lombok.Data
    public static class Data {

        private String paymentId;

        private SubMethod subMethod;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private Details details;

        @lombok.Data
        public static class Details {

            private String wallet;

            private String comment;
        }

        @lombok.Data
        public static class SubMethod {

            private Names names;

            @lombok.Data
            public static class Names {

                private String ru;
            }
        }
    }

    @AllArgsConstructor
    @Getter
    public enum ResponseStatus {
        DETAILS_NOT_FOUND("details_not_found"),
        DETAILS_FOUND("details_found"),
        ERROR("error");

        private final String value;

        public static ResponseStatus fromValue(String value) {
            for (ResponseStatus responseStatus : ResponseStatus.values()) {
                if (responseStatus.getValue().equals(value)) {
                    return responseStatus;
                }
            }
            return null;
        }

        public static class Deserializer extends JsonDeserializer<ResponseStatus> {

            @Override
            public ResponseStatus deserialize(JsonParser p, DeserializationContext ctxt) throws IOException {
                return ResponseStatus.fromValue(p.getValueAsString());
            }
        }
    }
}
