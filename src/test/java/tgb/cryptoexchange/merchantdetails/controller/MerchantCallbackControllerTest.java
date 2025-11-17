package tgb.cryptoexchange.merchantdetails.controller;

import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

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
        mockMvc.perform(post("/callback")
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
        mockMvc.perform(post("/callback")
                        .queryParam("merchant", merchant.name())
                        .queryParam("secret", "test-callback-secret")
                        .content(body))
                .andExpect(status().isOk());
        verify(merchantDetailsService).updateStatus(merchant, body);
    }
}