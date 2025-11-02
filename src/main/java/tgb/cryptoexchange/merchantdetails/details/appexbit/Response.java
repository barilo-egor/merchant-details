package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantResponse;

import java.util.List;
import java.util.Objects;

@Data
public class Response implements MerchantResponse {

    private Boolean success;

    private List<Offer> addedOffers;

    @Override
    public boolean hasDetails() {
        return Boolean.TRUE.equals(success) && Objects.nonNull(addedOffers) && !addedOffers.isEmpty()
                && Objects.nonNull(addedOffers.getFirst().getId()) && Objects.nonNull(addedOffers.getFirst().getMessage());
    }

    @Data
    public static class Offer {

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String id;

        private String message;
    }
}
