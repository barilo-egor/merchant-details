package tgb.cryptoexchange.merchantdetails.details.appexbit;

import com.fasterxml.jackson.databind.annotation.JsonDeserialize;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.List;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private Boolean success;

    private List<Offer> addedOffers;

    @Override
    public ValidationResult validate() {
        ValidationResult validationResult = new ValidationResult();
        if (Objects.isNull(success)) {
            validationResult.notNull("success");
        }
        if (Objects.nonNull(addedOffers) && addedOffers.size() !=1) {
            validationResult.notExpectedSize("addedOffers", 1, addedOffers.size());
        } else if (Objects.nonNull(addedOffers)) {
            Offer offer = addedOffers.getFirst();
            if (Objects.isNull(offer)) {
                validationResult.notNull("offer");
            } else if (Objects.isNull(offer.getId())) {
                validationResult.notNull("offer.id");
            } else if (Objects.isNull(offer.getStatus())) {
                validationResult.notNull("offer.status");
            }
        }
        return validationResult;
    }

    @Override
    public boolean hasDetails() {
        String message = addedOffers.getFirst().getMessage();
        return Objects.nonNull(message) && !message.isEmpty();
    }

    @Data
    public static class Offer {

        @JsonDeserialize(using = Status.Deserializer.class)
        private Status status;

        private String id;

        private String message;
    }
}
