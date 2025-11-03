package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;

import java.util.List;

@Data
public class Response {

    private Boolean success;

    private List<Offer> addedOffers;

    @Data
    public static class Offer {

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String id;

        private String message;
    }
}
