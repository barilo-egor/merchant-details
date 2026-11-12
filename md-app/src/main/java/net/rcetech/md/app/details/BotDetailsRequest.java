package net.rcetech.md.app.details;

import net.rcetech.md.integrations.platform.MerchantMethod;
import com.fasterxml.jackson.annotation.JsonAlias;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import tgb.cryptoexchange.commons.enums.Merchant;

import java.util.Collections;
import java.util.List;

@Data
@JsonIgnoreProperties(ignoreUnknown = true)
public class BotDetailsRequest implements DetailsRequest {

    protected String requestId;

    protected String id;

    @NotNull
    @Min(1)
    protected Integer amount;

    @JsonAlias("chatId")
    protected String userId;

    protected List<MerchantMethod> methods;

    protected String initiatorApp;

    protected String redirectUrl;

    protected String phone;

    @JsonIgnore
    public List<String> getMerchantMethod(Merchant merchant) {
        for (MerchantMethod merchantMethod : methods) {
            if (merchantMethod.getMerchant().equals(merchant)) {
                return merchantMethod.getMethods();
            }
        }
        return Collections.emptyList();
    }

    @Data
    @Builder
    @AllArgsConstructor
    @NoArgsConstructor
    public static class MerchantMethod {

        private Merchant merchant;

        private List<String> methods;

    }
}
