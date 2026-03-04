package tgb.cryptoexchange.merchantdetails.details.yolo;

import lombok.Data;

import java.time.Instant;

@Data
public class JwtData {

    private String accessToken;

    private Instant expiresAt;

}