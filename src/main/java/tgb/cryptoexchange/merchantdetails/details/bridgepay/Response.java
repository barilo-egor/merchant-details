package tgb.cryptoexchange.merchantdetails.details.bridgepay;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.details.MerchantDetailsResponse;
import tgb.cryptoexchange.merchantdetails.details.ValidationResult;

import java.util.List;
import java.util.Objects;

@Data
public class Response implements MerchantDetailsResponse {

    private String id;

    private Sum sum;

    private List<DealDTO> deals;

    @Override
    public ValidationResult validate() {
        ValidationResult result = new ValidationResult();
        if (Objects.isNull(id)) {
            result.notNull("id");
        }
        if (Objects.nonNull(deals) && !deals.isEmpty()) {
            DealDTO deal = deals.getFirst();
            if (Objects.isNull(deal)) {
                result.notNull("deal");
            }
            if (Objects.isNull(deal.getPaymentMethod())) {
                result.notNull("deal.paymentMethod");
            }
            if (Objects.isNull(deal.getRequisites())) {
                result.notNull("deal.requisites");
            } else {
                if (Objects.isNull(deal.getRequisites().getRequisites())) {
                    result.notNull("deal.requisites.requisites");
                }
            }
        }
        return result;
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(deals) && !deals.isEmpty();
    }

    @Data
    public static class Sum {

        private String amount;
    }
}
