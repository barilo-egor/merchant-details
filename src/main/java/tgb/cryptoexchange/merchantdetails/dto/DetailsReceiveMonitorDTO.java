package tgb.cryptoexchange.merchantdetails.dto;

import lombok.Data;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import java.time.Instant;
import java.util.List;

@Data
public class DetailsReceiveMonitorDTO {

    private Long dealId;

    private Integer amount;

    private Instant startTime;

    private Instant endTime;

    private boolean success;

    private List<MerchantAttempt> attempts;

    @Data
    public static class MerchantAttempt {

        private Merchant merchant;

        private String method;

        private Instant startTime;

        private Instant endTime;

        private boolean success;

        private boolean error;
    }
}
