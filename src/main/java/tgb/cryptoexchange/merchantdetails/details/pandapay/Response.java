package tgb.cryptoexchange.merchantdetails.details.pandapay;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private String uuid;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    @JsonProperty("requisite_data")
    private RequisiteData requisiteData;

    @Data
    public static class RequisiteData {

        @JsonProperty("bank_name_ru")
        private String bank;

        @JsonProperty("type")
        @JsonDeserialize(using = Method.Deserializer.class)
        private Method method;

        private String requisites;
    }
}
