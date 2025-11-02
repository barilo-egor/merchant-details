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

    @Bean
    public WebClient onyxPayWebClient(OnyxPayProperties onyxPayProperties) {
        return WebClient.builder().baseUrl(onyxPayProperties.url()).build();
    }

    @Bean
    public WebClient crocoPayWebClient(CrocoPayProperties crocoPayProperties) {
        return WebClient.builder().baseUrl(crocoPayProperties.url()).build();
    }

    @Bean
    public WebClient daoPaymentsWebClient(DaoPaymentsProperties daoPaymentsProperties) {
        return WebClient.builder().baseUrl(daoPaymentsProperties.url()).build();
    }

    @Bean
    public WebClient evoPayWebClient(EvoPayProperties evoPayProperties) {
        return WebClient.builder().baseUrl(evoPayProperties.url()).build();
    }

    @Bean
    public WebClient extasyPayWebClient(ExtasyPayProperties extasyPayProperties) {
        return WebClient.builder().baseUrl(extasyPayProperties.url()).build();
    }

    @Bean
    public WebClient yaPayWebClient(YaPayProperties yaPayProperties) {
        return WebClient.builder().baseUrl(yaPayProperties.url()).build();
    }

    @Bean
    public WebClient payPointsWebClient(PayPointsProperties payPointsProperties) {
        return WebClient.builder().baseUrl(payPointsProperties.url()).build();
    }

    @Bean
    public WebClient paradoxPayWebClient(ParadoxPayProperties paradoxPayProperties) {
        return WebClient.builder().baseUrl(paradoxPayProperties.url()).build();
    }

    @Bean
    public WebClient mobiusWebClient(MobiusProperties mobiusProperties) {
        return WebClient.builder().baseUrl(mobiusProperties.url()).build();
    }

    @Bean
    public WebClient foxPaysWebClient(FoxPaysProperties foxPaysProperties) {
        return WebClient.builder().baseUrl(foxPaysProperties.url()).build();
    }

    @Bean
    public WebClient worldWidePaymentsSystemsWebClient(WorldWidePaymentSystemsProperties worldWidePaymentSystemsProperties) {
        return WebClient.builder().baseUrl(worldWidePaymentSystemsProperties.url()).build();
    }

    @Bean
    public WebClient ezePayWebClient(EzePayProperties ezePayProperties) {
        return WebClient.builder().baseUrl(ezePayProperties.url()).build();
    }

    @Bean
    public WebClient honeyMoneyWebClient(HoneyMoneyProperties honeyMoneyProperties) {
        return WebClient.builder().baseUrl(honeyMoneyProperties.url()).build();
    }

    @Bean
    public WebClient payscrowWebClient(PayscrowPropertiesImpl payscrowProperties) {
        return WebClient.builder().baseUrl(payscrowProperties.url()).build();
    }

    @Bean
    public WebClient luckyPayWebClient(LuckyPayProperties luckyPayProperties) {
        return WebClient.builder().baseUrl(luckyPayProperties.url()).build();
    }

    @Bean
    public WebClient nicePayWebClient(NicePayProperties nicePayProperties) {
        return WebClient.builder().baseUrl(nicePayProperties.url()).build();
    }

    @Bean
    public WebClient onlyPaysWebClient(OnlyPaysProperties onlyPaysProperties) {
        return WebClient.builder().baseUrl(onlyPaysProperties.url()).build();
    }
}
