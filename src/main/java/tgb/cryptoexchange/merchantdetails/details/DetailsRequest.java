package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;

import java.util.List;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsRequest {

    private Long id;

    @NotNull
    @Min(1)
    private Integer amount;

    private Long chatId;

    private String initiatorApp;

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
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MerchantMethod {

        private Merchant merchant;

        private String method;
    }
}
