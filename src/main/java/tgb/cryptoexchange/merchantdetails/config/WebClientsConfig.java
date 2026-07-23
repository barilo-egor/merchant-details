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
    public WebClient crocoPayWebClient(CrocoPayImplProperties crocoPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(crocoPayProperties.url()).build();
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
    public WebClient extasyPayReceiptTriplePlusWebClient(ExtasyPayReceiptTriplePlusProperties extasyPayReceiptTriplePlusProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(extasyPayReceiptTriplePlusProperties.url()).build();
    }

    @Bean
    public WebClient extasyPayQRWebClient(ExtasyPayQRProperties extasyPayQRProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(extasyPayQRProperties.url()).build();
    }

    @Bean
    public WebClient pwPayWebClient(PwPayProperties pwPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(pwPayProperties.url()).build();
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
    public WebClient onlyPaysWebClient(OnlyPaysProperties onlyPaysProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(onlyPaysProperties.url()).build();
    }

    @Bean
    public WebClient payLeeWebClient(PayLeePropertiesImpl payLeeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(payLeeProperties.url()).build();
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
    public WebClient cashOutWebClient(CashOutProperties cashOutProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(cashOutProperties.url()).build();
    }

    @Bean
    public WebClient goatxWebClient(GoatxPropertiesImpl goatxProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(goatxProperties.url()).build();
    }

    @Bean
    public WebClient goatxSimWebClient(GoatxSimProperties goatxSimProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(goatxSimProperties.url()).build();
    }

    @Bean
    public WebClient lotrienWebClient(LotrienProperties lotrienProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(lotrienProperties.url()).build();
    }

    @Bean
    public WebClient gambitWebClient(GambitImplProperties gambitProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(gambitProperties.url()).build();
    }

    @Bean
    public WebClient gambitSimWebClient(GambitSimProperties gambitSimProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(gambitSimProperties.url()).build();
    }

    @Bean
    public WebClient asgardWebClient(AsgardImplProperties asgardProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public WebClient asgardWTWebClient(AsgardWTProperties asgardProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public WebClient asgardSimWebClient(AsgardSimProperties asgardProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public WebClient asgardHighCheckWebClient(AsgardHighCheckProperties asgardProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public WebClient deoraWebClient(DeoraProperties deoraProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public WebClient deoraSimWebClient(DeoraSimProperties deoraProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public WebClient deoraLowCheckWebClient(DeoraLowCheckProperties deoraProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public WebClient deoraPdfWebClient(DeoraPdfProperties deoraProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public WebClient meridianPayWebClient(MeridianPayProperties meridianPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public WebClient meridianPayHighCheckWebClient(MeridianPayHighCheckProperties meridianPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public WebClient meridianPayLowCheckWebClient(MeridianPayLowCheckProperties meridianPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public WebClient meridianPaySimWebClient(MeridianPaySimProperties meridianPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public WebClient meridianPayNspkWebClient(MeridianPayNspkProperties meridianPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public WebClient souzWebClient(SouzProperties souzProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public WebClient souzSbpQrWebClient(SouzSbpQrProperties souzProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public WebClient souzSimWebClient(SouzSimProperties souzProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public WebClient souzPdfWebClient(SouzPdfProperties souzProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public WebClient base51WebClient(Base51Properties base51Properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public WebClient base51SimWebClient(Base51SimProperties base51Properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public WebClient base51HighCheckWebClient(Base51HighCheckProperties base51Properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public WebClient base51LowCheckWebClient(Base51LowCheckProperties base51Properties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public WebClient bucksPaySimWebClient(BucksPaySimProperties bucksPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(bucksPayProperties.url()).build();
    }

    @Bean
    public WebClient bucksPayWebClient(BucksPayPropertiesImpl bucksPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(bucksPayProperties.url()).build();
    }

    @Bean
    public WebClient manyPayWebClient(ManyPayPropertiesImpl manyPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(manyPayProperties.url()).build();
    }

    @Bean
    public WebClient manyPayLowCheckWebClient(ManyPayLowCheckProperties manyPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(manyPayProperties.url()).build();
    }

    @Bean
    public WebClient manyPayHighCheckWebClient(ManyPayHighCheckProperties manyPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(manyPayProperties.url()).build();
    }

    @Bean
    public WebClient prismaPayWebClient(PrismaPayProperties prismaPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(prismaPayProperties.url()).build();
    }

    @Bean
    public WebClient paySyncWebClient(PaySyncProperties paySyncProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(paySyncProperties.url()).build();
    }

    @Bean
    public WebClient cubeWebClient(CubePropertiesImpl cubeProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(cubeProperties.url()).build();
    }

    @Bean
    public WebClient rsPayWebClient(RSPayImplProperties rsPayProperties) {
        return get30SecondsResponseTimeoutWebClientBuilder().baseUrl(rsPayProperties.url()).build();
    }
}
