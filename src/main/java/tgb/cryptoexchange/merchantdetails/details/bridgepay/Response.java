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
        if (hasDetails()) {
            if (Objects.isNull(id)) {
                result.notNull("id");
            }
            if (Objects.isNull(deals.getFirst())) {
                result.notNull("deal");
            } else {
                validateDeal(result);
            }
        }
        return result;
    }

    private void validateDeal(ValidationResult result) {
        DealDTO deal = deals.getFirst();
        if (Objects.isNull(deal.getPaymentMethod())) {
            result.notNull("deal.paymentMethod");
        }
        if (Method.SBP_QR.equals(deal.getPaymentOption()) && Objects.isNull(deal.getQrCodeLink())) {
            result.notNull("deal.qrCodeLink");
            return;
        }
        if (Objects.isNull(deal.getRequisites())) {
            result.notNull("deal.requisites");
        } else {
            if (Objects.isNull(deal.getRequisites().getRequisites())) {
                result.notNull("deal.requisites.requisites");
            }
        }
    }

    @Override
    public boolean hasDetails() {
        return Objects.nonNull(deals) && !deals.isEmpty() && hasQrCodeLinkIfSPBQR(deals.getFirst());
    }

    private boolean hasQrCodeLinkIfSPBQR(DealDTO dealDTO) {
        if (Method.SBP_QR.equals(dealDTO.getPaymentOption())) {
            return !(dealDTO.getQrCodeLink().endsWith("qript.ru") || dealDTO.getQrCodeLink().startsWith("https://"));
        }
        return true;
    }

    @Data
    public static class Sum {

        private String amount;

    }

}
