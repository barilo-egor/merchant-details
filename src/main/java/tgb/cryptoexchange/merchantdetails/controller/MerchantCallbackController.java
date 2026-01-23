package tgb.cryptoexchange.merchantdetails.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.commons.enums.Merchant;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.details.crocopay.Callback;
import tgb.cryptoexchange.merchantdetails.details.crocopay.Status;
import tgb.cryptoexchange.merchantdetails.properties.PayLeePropertiesImpl;
import tgb.cryptoexchange.merchantdetails.service.CryptoService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.security.GeneralSecurityException;

@RestController
@RequestMapping("/merchant-details/callback")
@Slf4j
public class MerchantCallbackController extends ApiController {

    private final MerchantDetailsService merchantDetailsService;

    private final String secret;

    private final CryptoService cryptoService;

    private final PayLeePropertiesImpl payLeeProperties;

    private final ObjectMapper objectMapper;

    public MerchantCallbackController(MerchantDetailsService merchantDetailsService,
                                      @Value("${callback-secret}") String secret, ObjectMapper objectMapper,
                                      CryptoService cryptoService, PayLeePropertiesImpl payLeeProperties) {
        this.merchantDetailsService = merchantDetailsService;
        this.secret = secret;
        this.cryptoService = cryptoService;
        this.payLeeProperties = payLeeProperties;
        this.objectMapper = objectMapper;
    }

    @RequestMapping
    public ResponseEntity<Void> callback(@RequestParam Merchant merchant, @RequestParam String secret,
                                         @RequestBody String callbackBody) {
        if (!this.secret.equals(secret)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        if (Merchant.PAY_LEE.equals(merchant)) {
            try {
                log.debug("Callback мерчанта PAY_LEE до дешифровки: {}", callbackBody);
                callbackBody = cryptoService.decrypt(payLeeProperties.secret(), callbackBody);
            } catch (GeneralSecurityException e) {
                log.error("Ошибка преобразования токена PayLee: {} ", e.getMessage(), e);
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            }
        }
        merchantDetailsService.updateStatus(merchant, callbackBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/crocoPay")
    public ResponseEntity<Void> crocoPay(@RequestParam Long dealId, String secret) throws JsonProcessingException {
        if (!this.secret.equals(secret)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Callback callback = new Callback();
        callback.setId(dealId.toString());
        callback.setStatus(Status.SUCCESS);
        merchantDetailsService.updateStatus(Merchant.CROCO_PAY, objectMapper.writeValueAsString(callback));
        return new ResponseEntity<>(HttpStatus.OK);
    }
}
