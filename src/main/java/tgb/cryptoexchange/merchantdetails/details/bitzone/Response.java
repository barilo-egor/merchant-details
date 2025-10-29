package tgb.cryptoexchange.merchantdetails.details.bitzone;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

@Data
public class Response {

    private String id;

    @JsonDeserialize(using = Status.Deserializer.class)
    private Status status;

    private Requisite requisite;

    @JsonDeserialize(using = Method.Deserializer.class)
    private Method method;

    @Data
    public static class Requisite {

        private String bank;

        private String sbpNumber;

        private String requisites;
    }
}
