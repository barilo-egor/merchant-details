package tgb.cryptoexchange.merchantdetails.details.wellbit;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class Response {

    private Payment payment;

    @Data
    @JsonIgnoreProperties(ignoreUnknown = true)
    public static class Payment {
        private Long id;

        private String credential;

        @JsonProperty("credential_additional_bank")
        private String credentialAdditionalBank;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;
    }
}
