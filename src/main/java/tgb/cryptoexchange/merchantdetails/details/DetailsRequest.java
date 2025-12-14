package tgb.cryptoexchange.merchantdetails.details;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import java.util.List;
import java.util.Objects;
import java.util.Optional;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class DetailsRequest {

    private Long id;

    private String method;

    @NotNull
    @Min(1)
    private Integer amount;

    private Long chatId;

    private String initiatorApp;

    @NotEmpty
    private List<MerchantMethod> methods;

    public Optional<String> getMethod(Merchant merchant) {
        if (Objects.isNull(methods)) {
            return Optional.of(method);
        }
        for (MerchantMethod merchantMethod : methods) {
            if (merchantMethod.getMerchant().equals(merchant)) {
                return Optional.of(merchantMethod.getMethod());
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
