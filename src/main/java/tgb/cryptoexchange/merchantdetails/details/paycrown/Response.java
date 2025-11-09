package tgb.cryptoexchange.merchantdetails.details.paycrown;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.io.IOException;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Data data;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (hasDetails()) {
            if (Objects.isNull(data.id)) {
                result.notNull("data.id");
            }
            if (Objects.isNull(data.requisites.bank)) {
                result.notNull("data.requisites.bank");
            }
            if (Objects.isNull(data.requisites.requisitesString)) {
                result.notNull("data.requisites.requisitesString");
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(data) && Objects.nonNull(data.getRequisites());
    }

    @lombok.Data
    public static class Data {

        private String id;

        @JsonDeserialize(using = RequisitesDeserializer.class)
        private Requisites requisites;

        @lombok.Data
        public static class Requisites {

            private String bank;

            @JsonProperty("requisites")
            private String requisitesString;
        }

        public static class RequisitesDeserializer extends JsonDeserializer<Requisites> {
            @Override
            public Requisites deserialize(JsonParser jsonParser, DeserializationContext deserializationContext) throws IOException {
                if (jsonParser.getCurrentToken() == JsonToken.START_ARRAY) {
                    return null;
                }
                return jsonParser.getCodec().readValue(jsonParser, Requisites.class);
            }
        }
    }
}
