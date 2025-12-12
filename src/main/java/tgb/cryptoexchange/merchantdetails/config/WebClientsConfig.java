package tgb.cryptoexchange.merchantdetails.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.netty.http.client.HttpClient;
import tgb.cryptoexchange.merchantdetails.properties.*;

import java.time.Duration;

@Configuration
public class WebClientsConfig {

    public static WebClient.Builder get30SecondsResponseTimeoutWebClientBuilder() {
        return WebClient.builder()
                .clientConnector(
                        new ReactorClientHttpConnector(HttpClient.create().responseTimeout(Duration.ofSeconds(30)))
                );
    }

    @Bean
    public WebClient alfaTeamWebClient(AlfaTeamProperties alfaTeamProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(alfaTeamProperties.url()).build();
    }

    @Bean
    public WebClient geoTransferWebClient(GeoTransferProperties geoTransferProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(geoTransferProperties.url()).build();
    }

    @Bean
    public WebClient rostrastWebClient(RostrastProperties rostrastProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(rostrastProperties.url()).build();
    }

    @Bean
    public WebClient appexbitWebClient(AppexbitProperties appexbitProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(appexbitProperties.url()).build();
    }

    @Bean
    public WebClient bitZoneWebClient(BitZoneProperties bitZoneProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(bitZoneProperties.url()).build();
    }

    @Bean
    public WebClient onyxPayWebClient(OnyxPayProperties onyxPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(onyxPayProperties.url()).build();
    }

    @Bean
    public WebClient crocoPayWebClient(CrocoPayProperties crocoPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(crocoPayProperties.url()).build();
    }

    @Bean
    public WebClient daoPaymentsWebClient(DaoPaymentsProperties daoPaymentsProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(daoPaymentsProperties.url()).build();
    }

    @Bean
    public WebClient evoPayWebClient(EvoPayProperties evoPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(evoPayProperties.url()).build();
    }

    @Bean
    public WebClient extasyPayWebClient(ExtasyPayProperties extasyPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(extasyPayProperties.url()).build();
    }

    @Bean
    public WebClient yaPayWebClient(YaPayProperties yaPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(yaPayProperties.url()).build();
    }

    @Bean
    public WebClient mobiusWebClient(MobiusProperties mobiusProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(mobiusProperties.url()).build();
    }

    @Bean
    public WebClient foxPaysWebClient(FoxPaysProperties foxPaysProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(foxPaysProperties.url()).build();
    }

    @Bean
    public WebClient honeyMoneyWebClient(HoneyMoneyProperties honeyMoneyProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(honeyMoneyProperties.url()).build();
    }

    @Bean
    public WebClient payscrowWebClient(PayscrowPropertiesImpl payscrowProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payscrowProperties.url()).build();
    }

    @Bean
    public WebClient luckyPayWebClient(LuckyPayProperties luckyPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(luckyPayProperties.url()).build();
    }

    @Bean
    public WebClient nicePayWebClient(NicePayProperties nicePayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(nicePayProperties.url()).build();
    }

    @Bean
    public WebClient onlyPaysWebClient(OnlyPaysProperties onlyPaysProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(onlyPaysProperties.url()).build();
    }

    @Bean
    public WebClient payCrownWebClient(PayCrownProperties payCrownProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payCrownProperties.url()).build();
    }

    @Bean
    public WebClient payLeeWebClient(PayLeeProperties payLeeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payLeeProperties.url()).build();
    }

    @Bean
    public WebClient pspWareWebClient(PspWareProperties pspWareProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(pspWareProperties.url()).build();
    }

    @Bean
    public WebClient wellBitWebClient(WellBitProperties wellBitProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(wellBitProperties.url()).build();
    }

    @Bean
    public WebClient stormTradeWebClient(StormTradeProperties stormTradeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(stormTradeProperties.url()).build();
    }

    @Bean
    public WebClient settleXWebClient(SettleXProperties settleXProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(settleXProperties.url()).build();
    }

    @Bean
    public WebClient auroraWebClient(AuroraPayProperties auroraPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(auroraPayProperties.url()).build();
    }
}
