package tgb.cryptoexchange.merchantdetails.details;

import lombok.Data;
import lombok.EqualsAndHashCode;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.List;
import java.util.Optional;

@EqualsAndHashCode(callSuper = true)
@Data
public class MethodsDetailsRequest extends CommonDetailsRequest {

    private List<MerchantMethod> methods;

    public Optional<String> getMethod(Merchant merchant) {
        for (MerchantMethod method : methods) {
            if (method.getMerchant().equals(merchant)) {
                return Optional.of(method.getMethod());
            }
        }
        return Optional.empty();
    }

    @Data
    public static class MerchantMethod {

        private Merchant merchant;

        private String method;
    }
}
