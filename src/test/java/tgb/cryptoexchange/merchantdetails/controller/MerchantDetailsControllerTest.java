package tgb.cryptoexchange.merchantdetails.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.ArgumentCaptor;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tgb.cryptoexchange.merchantdetails.details.CancelOrderRequest;
import tgb.cryptoexchange.merchantdetails.details.DetailsResponse;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AppexbitProperties;
import tgb.cryptoexchange.merchantdetails.properties.MerchantPropertiesService;
import tgb.cryptoexchange.merchantdetails.service.MerchantDetailsService;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MerchantDetailsController.class)
@ExtendWith(MockitoExtension.class)
class MerchantDetailsControllerTest {

    @MockitoBean
    private MerchantPropertiesService merchantPropertiesService;

    @MockitoBean
    private MerchantDetailsService merchantDetailsService;

    @Autowired
    private MockMvc mockMvc;

    @ParameterizedTest
    @CsvSource({
            "https://google.com,someKey",
            "https://youtube.com,someAnotherYoutubeKeyWith123Numbers"
    })
    void shouldReturnJsonProperties(String url, String key) throws Exception {
        var appexbitProperties = new AppexbitProperties(url, key);
        when(merchantPropertiesService.getProperties(Merchant.APPEXBIT)).thenReturn(Optional.of(appexbitProperties));
        mockMvc.perform(get("/merchant-details/properties/APPEXBIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value(url))
                .andExpect(jsonPath("$.data.key").value(key));
    }

    @Test
    void shouldReturn404IfHasNoProperties() throws Exception {
        when(merchantPropertiesService.getProperties(Merchant.APPEXBIT)).thenReturn(Optional.empty());
        mockMvc.perform(get("/merchant-details/properties/APPEXBIT"))
                .andExpect(status().isNotFound());
    }

    @Test
    void detailsShouldReturn204IfNoDetails() throws Exception {
        when(merchantDetailsService.getDetails(any(), any())).thenReturn(Optional.empty());
        mockMvc.perform(get("/merchant-details/ALFA_TEAM")
                        .param("method", "value")
                        .param("amount", "1000"))
                .andExpect(status().isNoContent());
    }

    @CsvSource(nullValues = "null", textBlock = """
            ALFA_TEAM,ALFA 79878764521,3b8cd3a8-28dc-49a9-a522-91c582c83b5f,PENDING,\
            b98747be-0060-4b13-aee6-f7d489f23231,5444,\
            c29tZSBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGZpbGU=
            WELL_BIT,Сбербанк 1234123412341234,f669eb83-a6c2-4456-8416-c2b1fd514c99,CREATED,\
            c7a29fbf-7c80-44dd-8781-4117165569dc,2055,\
            c29tZSAybG9uZyAybG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgbG9uZyBsb25nIGxvbmcgZmlsZSA=
            """)
    @ParameterizedTest
    void detailsShouldReturnDetailsResponse(Merchant merchant, String details, String merchantOrderId,
                                            String merchantOrderStatus, String merchantCustomId, Integer amount,
                                            String qr) throws Exception {
        DetailsResponse detailsResponse = new DetailsResponse();
        detailsResponse.setMerchant(merchant);
        detailsResponse.setDetails(details);
        detailsResponse.setMerchantOrderId(merchantOrderId);
        detailsResponse.setMerchantOrderStatus(merchantOrderStatus);
        detailsResponse.setMerchantCustomId(merchantCustomId);
        detailsResponse.setAmount(amount);
        detailsResponse.setQr(qr);
        when(merchantDetailsService.getDetails(any(), any())).thenReturn(Optional.of(detailsResponse));
        mockMvc.perform(get("/merchant-details/ALFA_TEAM")
                        .param("method", "value")
                        .param("amount", "1000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.merchant").value(merchant.name()))
                .andExpect(jsonPath("$.data.details").value(details))
                .andExpect(jsonPath("$.data.merchantOrderId").value(merchantOrderId))
                .andExpect(jsonPath("$.data.merchantOrderStatus").value(merchantOrderStatus))
                .andExpect(jsonPath("$.data.merchantCustomId").value(merchantCustomId))
                .andExpect(jsonPath("$.data.amount").value(amount))
                .andExpect(jsonPath("$.data.qr").value(qr));
    }

    @CsvSource("""
            ALFA_TEAM,f669eb83-a6c2-4456-8416-c2b1fd514c99,CARD
            ONLY_PAYS,3b8cd3a8-28dc-49a9-a522-91c582c83b5f,SBP
            """)
    @ParameterizedTest
    void cancelShouldCallCancelOrderMethod(Merchant merchant, String orderId, String method) throws Exception {
        mockMvc.perform(patch("/merchant-details/" + merchant.name())
                .queryParam("orderId", orderId)
                .queryParam("method", method)
        ).andExpect(status().isOk());
        ArgumentCaptor<CancelOrderRequest> cancelOrderRequestCaptor = ArgumentCaptor.forClass(CancelOrderRequest.class);
        verify(merchantDetailsService).cancelOrder(eq(merchant), cancelOrderRequestCaptor.capture());
        CancelOrderRequest actual = cancelOrderRequestCaptor.getValue();
        assertAll(
                () -> assertEquals(orderId, actual.getOrderId()),
                () -> assertEquals(method, actual.getMethod())
        );
    }
}