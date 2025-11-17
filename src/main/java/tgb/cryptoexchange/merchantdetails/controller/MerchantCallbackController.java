package tgb.cryptoexchange.merchantdetails.controller;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.controller.ApiController;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

@RestController
@RequestMapping("/callback")
public class MerchantCallbackController extends ApiController {

    private final MerchantDetailsService merchantDetailsService;

    public MerchantCallbackController(MerchantDetailsService merchantDetailsService) {
        this.merchantDetailsService = merchantDetailsService;
    }

    @ResponseStatus(HttpStatus.OK)
    @PostMapping
    public void callback(@RequestParam Merchant merchant, @RequestBody String callbackBody) {
        merchantDetailsService.updateStatus(merchant, callbackBody);
    }
}
