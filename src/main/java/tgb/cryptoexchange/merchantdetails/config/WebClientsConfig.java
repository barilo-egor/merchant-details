package tgb.cryptoexchange.merchantdetails.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;
import tgb.cryptoexchange.merchantdetails.properties.*;

@Configuration
public class WebClientsConfig {

    @Bean
    public WebClient alfaTeamWebClient(AlfaTeamProperties alfaTeamProperties) {
        return WebClient.builder().baseUrl(alfaTeamProperties.url()).build();
    }

    @Bean
    public WebClient geoTransferWebClient(GeoTransferProperties geoTransferProperties) {
        return WebClient.builder().baseUrl(geoTransferProperties.url()).build();
    }

    @Bean
    public WebClient rostrastWebClient(RostrastProperties rostrastProperties) {
        return WebClient.builder().baseUrl(rostrastProperties.url()).build();
    }

    @Bean
    public WebClient appexbitWebClient(AppexbitProperties appexbitProperties) {
        return WebClient.builder().baseUrl(appexbitProperties.url()).build();
    }

    @Bean
    public WebClient bitZoneWebClient(BitZoneProperties bitZoneProperties) {
        return WebClient.builder().baseUrl(bitZoneProperties.url()).build();
    }
}
