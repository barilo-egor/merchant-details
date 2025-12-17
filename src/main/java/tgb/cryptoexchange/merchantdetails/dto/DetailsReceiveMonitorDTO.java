package tgb.cryptoexchange.merchantdetails.dto;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.datatype.jsr310.ser.InstantSerializer;
import lombok.Data;
import tgb.cryptoexchange.merchantdetails.constants.Merchant;

import java.time.Instant;
import java.util.List;

@Data
public class DetailsReceiveMonitorDTO {

    private Long dealId;

    private Integer amount;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant startTime;

    @JsonSerialize(using = InstantSerializer.class)
    private Instant endTime;

    private boolean success;

    private List<MerchantAttempt> attempts;

    @Data
    public static class MerchantAttempt {

        private Merchant merchant;

        private String method;

        @JsonSerialize(using = InstantSerializer.class)
        private Instant startTime;

        @JsonSerialize(using = InstantSerializer.class)
        private Instant endTime;

        private boolean success;

        private boolean error;
    }
}
