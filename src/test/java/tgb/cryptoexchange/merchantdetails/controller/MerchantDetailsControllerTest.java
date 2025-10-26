package tgb.cryptoexchange.merchantdetails.controller;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import tgb.cryptoexchange.merchantdetails.enums.Merchant;
import tgb.cryptoexchange.merchantdetails.properties.AppexbitProperties;
import tgb.cryptoexchange.merchantdetails.properties.MerchantPropertiesService;

import java.util.Optional;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(controllers = MerchantDetailsController.class)
@ExtendWith(MockitoExtension.class)
class MerchantDetailsControllerTest {

    @MockitoBean
    private MerchantPropertiesService merchantPropertiesService;

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
        mockMvc.perform(get("/properties/APPEXBIT"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.url").value(url))
                .andExpect(jsonPath("$.data.key").value(key));
    }

    @Test
    void shouldReturn404IfHasNoProperties() throws Exception {
        when(merchantPropertiesService.getProperties(Merchant.APPEXBIT)).thenReturn(Optional.empty());
        mockMvc.perform(get("/properties/APPEXBIT"))
                .andExpect(status().isNotFound());
    }
}