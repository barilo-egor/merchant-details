package tgb.cryptoexchange.merchantdetails.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MerchantCallbackController.class)
@ExtendWith(MockitoExtension.class)
class MerchantCallbackControllerTest {

    @MockitoBean
    private MerchantDetailsService merchantDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @ValueSource(strings = {
            "qwerty123", "f820e164-97f9-4098-9b4d-74707bcbee1b", ""
    })
    @ParameterizedTest
    void callbackShouldReturn403IfWrongSecret(String secret) throws Exception {
        mockMvc.perform(post("/merchant-details/callback")
                        .queryParam("merchant", Merchant.ALFA_TEAM.name())
                        .queryParam("secret", secret)
                        .content("body"))
                .andExpect(status().isForbidden());
    }

    @CsvSource(delimiter = ';', textBlock = """
            ALFA_TEAM;{"someField":"someValue"}
            WELL_BIT;{"someField2":"someValue2"}
            """)
    @ParameterizedTest
    void callbackShouldReturn200IfSecretAccessed(Merchant merchant, String body) throws Exception {
        mockMvc.perform(post("/merchant-details/callback")
                        .queryParam("merchant", merchant.name())
                        .queryParam("secret", "test-callback-secret")
                        .content(body))
                .andExpect(status().isOk());
        verify(merchantDetailsService).updateStatus(merchant, body);
    }

    @ValueSource(strings = {
            "qwerty123", "f820e164-97f9-4098-9b4d-74707bcbee1b", ""
    })
    @ParameterizedTest
    void crocoPayShouldReturn403IfWrongSecret(String secret) throws Exception {
        mockMvc.perform(post("/merchant-details/callback/crocoPay")
                        .queryParam("dealId", String.valueOf(1000))
                        .queryParam("secret", secret)
                        .content("body"))
                .andExpect(status().isForbidden());
    }

    @CsvSource(textBlock = """
            {"someField":"someValue"},52915
            {"someField2":"someValue2"},12506
            """)
    @ParameterizedTest
    void crocoPayShouldReturn200IfSecretAccessed(String body, Long dealId) throws Exception {
        mockMvc.perform(post("/merchant-details/callback/crocoPay")
                        .queryParam("secret", "test-callback-secret")
                        .queryParam("dealId", String.valueOf(dealId))
                        .content(body))
                .andExpect(status().isOk());
        ArgumentCaptor<String> bodyCaptor = ArgumentCaptor.forClass(String.class);
        verify(merchantDetailsService).updateStatus(eq(Merchant.CROCO_PAY), bodyCaptor.capture());
        assertEquals("{\"id\":\"" + dealId + "\",\"status\":\"SUCCESS\"}", bodyCaptor.getValue());
    }
}