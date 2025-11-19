package tgb.cryptoexchange.merchantdetails.controller;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

@RestController
@RequestMapping("/merchant-details/callback")
public class MerchantCallbackController extends ApiController {

    private final MerchantDetailsService merchantDetailsService;

    private final String secret;

    public MerchantCallbackController(MerchantDetailsService merchantDetailsService, @Value("${callback-secret}") String secret) {
        this.merchantDetailsService = merchantDetailsService;
        this.secret = secret;
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
}
