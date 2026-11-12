package net.rcetech.md.callback.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import tgb.cryptoexchange.commons.enums.Merchant;
import net.rcetech.md.callback.callback.MerchantCallbackService;
import net.rcetech.md.integrations.platform.CallbackDecryptService;
import net.rcetech.md.integrations.platform.crocopay.Callback;
import net.rcetech.md.integrations.platform.crocopay.Status;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/merchant-details/callback")
@Slf4j
public class MerchantCallbackController {

    private final MerchantCallbackService merchantCallbackService;

    private final String secret;

    private final Map<Merchant, CallbackDecryptService> callbackDecryptServiceMap;

    private final ObjectMapper objectMapper;

    public MerchantCallbackController(MerchantCallbackService merchantCallbackService,
                                      @Value("${callback-secret}") String secret, ObjectMapper objectMapper,
                                      Map<Merchant, CallbackDecryptService> callbackDecryptServiceMap) {
        this.merchantCallbackService = merchantCallbackService;
        this.secret = secret;
        this.callbackDecryptServiceMap = callbackDecryptServiceMap;
        this.objectMapper = objectMapper;
    }

    @PostMapping
    @PatchMapping
    public ResponseEntity<Void> callback(@RequestParam Merchant merchant, @RequestParam String secret,
                                         @RequestBody String callbackBody) {
        if (!this.secret.equals(secret)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        if (callbackDecryptServiceMap.containsKey(merchant)) {
            callbackBody = callbackDecryptServiceMap.get(merchant).decrypt(callbackBody);
        }

        merchantCallbackService.updateStatus(merchant, callbackBody);
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping(value = "/{merchant}/fetch-status")
    public ResponseEntity<Void> callbackGet(@RequestParam String transactionId, @RequestParam String secret, @PathVariable String merchant) throws JacksonException {
        if (!this.secret.equals(secret)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }

        merchantCallbackService.fetchStatus(Merchant.valueOf(merchant), transactionId);


        return new ResponseEntity<>(HttpStatus.OK);
    }

    @PostMapping("/{merchant}")
    public ResponseEntity<Void> callbackPost(@RequestParam Long dealId, @RequestParam String secret, @PathVariable String merchant) throws JacksonException {
        var merchants = List.of(
                Merchant.CROCO_PAY.name(),
                Merchant.BASE_51.name(),
                Merchant.BASE_51_SIM.name(),
                Merchant.BASE_51_HIGH_CHECK.name(),
                Merchant.BASE_51_LOW_CHECK.name());
        if (!merchants.contains(merchant) || !this.secret.equals(secret)) {
            return new ResponseEntity<>(HttpStatus.FORBIDDEN);
        }
        Callback callback = new Callback();
        callback.setId(dealId.toString());
        callback.setStatus(Status.SUCCESS);
        merchantCallbackService.updateStatus(Merchant.valueOf(merchant), objectMapper.writeValueAsString(callback));
        return new ResponseEntity<>(HttpStatus.OK);
    }

    @GetMapping("/{merchant}")
    public ResponseEntity<Void> callbackGet(@RequestParam Long dealId, @RequestParam String secret, @PathVariable String merchant) throws JacksonException {
        return callbackPost(dealId, secret, merchant);
    }

}
