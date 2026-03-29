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
    public WebClient alfaTeamWTWebClient(AlfaTeamWTProperties alfaTeamWTProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(alfaTeamWTProperties.url()).build();
    }

    @Bean
    public WebClient alfaTeamQRWebClient(AlfaTeamQRProperties alfaTeamQRProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(alfaTeamQRProperties.url()).build();
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
    public WebClient extasyPayReceiptWebClient(ExtasyPayReceiptProperties extasyPayReceiptProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(extasyPayReceiptProperties.url()).build();
    }

    @Bean
    public WebClient extasyPayQRWebClient(ExtasyPayQRProperties extasyPayQRProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(extasyPayQRProperties.url()).build();
    }

    @Bean
    public WebClient yaPayWebClient(YaPayProperties yaPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(yaPayProperties.url()).build();
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
    public WebClient payscrowHighCheckWebClient(PayscrowHighCheckProperties payscrowHighCheckProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payscrowHighCheckProperties.url()).build();
    }

    @Bean
    public WebClient payscrowWTWebClient(PayscrowWTProperties payscrowWTProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payscrowWTProperties.url()).build();
    }

    @Bean
    public WebClient payscrowSimWebClient(PayscrowSimProperties payscrowSimProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payscrowSimProperties.url()).build();
    }

    @Bean
    public WebClient luckyPayWebClient(LuckyPayProperties luckyPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(luckyPayProperties.url()).build();
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
    public WebClient payLeeWebClient(PayLeePropertiesImpl payLeeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payLeeProperties.url()).build();
    }

    @Bean
    public WebClient pspWareWebClient(PspWareProperties pspWareProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(pspWareProperties.url()).build();
    }

    @Bean
    public WebClient stormTrade13WebClient(StormTrade13Properties stormTrade13Properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(stormTrade13Properties.url()).build();
    }

    @Bean
    public WebClient stormTradeWebClient(StormTradeProperties stormTradeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(stormTradeProperties.url()).build();
    }

    @Bean
    public WebClient settleXWebClient(SettleXPropertiesImpl settleXProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(settleXProperties.url()).build();
    }

    @Bean
    public WebClient settleX15WebClient(SettleX15Properties settleX15Properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(settleX15Properties.url()).build();
    }

    @Bean
    public WebClient plataPaymentWebClient(PlataPaymentProperties properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(properties.url()).build();
    }

    @Bean
    public WebClient plata18WebClient(Plata18Properties properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(properties.url()).build();
    }

    @Bean
    public WebClient payLeeQRWebClient(PayLeeQRProperties payLeeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payLeeProperties.url()).build();
    }

    @Bean
    public WebClient neuralPayWebClient(NeuralPayProperties neuralPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(neuralPayProperties.url()).build();
    }

    @Bean
    public WebClient studioWebClient(StudioProperties studioProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(studioProperties.url()).build();
    }

    @Bean
    public WebClient studioSimWebClient(StudioSimProperties studioSimProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(studioSimProperties.url()).build();
    }

    @Bean
    public WebClient yoloWebClient(YoloPropertiesImpl yoloProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(yoloProperties.url()).build();
    }

    @Bean
    public WebClient yoloSimWebClient(YoloSimProperties yoloSimProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(yoloSimProperties.url()).build();
    }

    @Bean
    public WebClient norosWebClient(NorosPropertiesImpl norosProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(norosProperties.url()).build();
    }

    @Bean
    public WebClient norosHighCheckWebClient(NorosHighCheckProperties norosHighCheckProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(norosHighCheckProperties.url()).build();
    }

    @Bean
    public WebClient fiatCutWebClient(FiatCutProperties fiatCutProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(fiatCutProperties.url()).build();
    }

    @Bean
    public WebClient viatrumWebClient(ViatrumProperties viatrumProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(viatrumProperties.url()).build();
    }

    @Bean
    public WebClient cashOutWebClient(CashOutProperties cashOutProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(cashOutProperties.url()).build();
    }
}
