package tgb.cryptoexchange.merchantdetails.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.details.crocopay.Callback;
import tgb.cryptoexchange.merchantdetails.details.crocopay.Status;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

@RestController
@RequestMapping("/merchant-details/callback")
public class MerchantCallbackController extends ApiController {

    private final MerchantDetailsService merchantDetailsService;

    private final String secret;

    private final ObjectMapper objectMapper;

    public MerchantCallbackController(MerchantDetailsService merchantDetailsService,
                                      @Value("${callback-secret}") String secret, ObjectMapper objectMapper) {
        this.merchantDetailsService = merchantDetailsService;
        this.secret = secret;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    public ResponseEntity<Void> callback(@RequestParam Merchant merchant, @RequestParam String secret,
                                         @RequestBody String callbackBody) {
        if (!this.secret.equals(secret)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
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
