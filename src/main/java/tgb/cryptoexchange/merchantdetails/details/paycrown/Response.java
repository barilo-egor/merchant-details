package tgb.cryptoexchange.merchantdetails.details.paycrown;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;
import com.fasterxml.jackson.databind.DeserializationContext;
import com.fasterxml.jackson.databind.JsonDeserializer;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.io.IOException;

@Data
public class Response {

    private Data data;

    @lombok.Data
    public static class Data {

        private String id;

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

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
