package net.rcetech.md.integrations.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;
import net.rcetech.md.integrations.properties.*;

import java.net.http.HttpClient;
import java.nio.charset.StandardCharsets;
import java.time.Duration;

@Configuration
public class RestClientsConfig {

    private static final Duration TIMEOUT = Duration.ofSeconds(30);

    /**
     * Возвращает билдер {@link RestClient} с таймаутами соединения и ожидания ответа в 30 секунд
     * и чтением/записью строковых тел в UTF-8 независимо от charset в Content-Type.
     *
     * @return билдер клиента
     */
    public static RestClient.Builder get30SecondsReadTimeoutRestClientBuilder() {
        JdkClientHttpRequestFactory requestFactory = new JdkClientHttpRequestFactory(
                HttpClient.newBuilder().connectTimeout(TIMEOUT).build()
        );
        requestFactory.setReadTimeout(TIMEOUT);
        return RestClient.builder()
                .requestFactory(requestFactory)
                .configureMessageConverters(converters ->
                        converters.registerDefaults()
                                .withStringConverter(new StringHttpMessageConverter(StandardCharsets.UTF_8)));
    }

    @Bean
    public RestClient alfaTeamRestClient(AlfaTeamProperties alfaTeamProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(alfaTeamProperties.url()).build();
    }

    @Bean
    public RestClient alfaTeamWTRestClient(AlfaTeamWTProperties alfaTeamWTProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(alfaTeamWTProperties.url()).build();
    }

    @Bean
    public RestClient alfaTeamQRRestClient(AlfaTeamQRProperties alfaTeamQRProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(alfaTeamQRProperties.url()).build();
    }

    @Bean
    public RestClient geoTransferRestClient(GeoTransferProperties geoTransferProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(geoTransferProperties.url()).build();
    }

    @Bean
    public RestClient rostrastRestClient(RostrastProperties rostrastProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(rostrastProperties.url()).build();
    }

    @Bean
    public RestClient bitZoneRestClient(BitZoneProperties bitZoneProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(bitZoneProperties.url()).build();
    }

    @Bean
    public RestClient onyxPayRestClient(OnyxPayProperties onyxPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(onyxPayProperties.url()).build();
    }

    @Bean
    public RestClient crocoPayRestClient(CrocoPayImplProperties crocoPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(crocoPayProperties.url()).build();
    }

    @Bean
    public RestClient evoPayRestClient(EvoPayProperties evoPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(evoPayProperties.url()).build();
    }

    @Bean
    public RestClient extasyPayRestClient(ExtasyPayProperties extasyPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(extasyPayProperties.url()).build();
    }

    @Bean
    public RestClient extasyPayReceiptRestClient(ExtasyPayReceiptProperties extasyPayReceiptProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(extasyPayReceiptProperties.url()).build();
    }

    @Bean
    public RestClient extasyPayReceiptTriplePlusRestClient(ExtasyPayReceiptTriplePlusProperties extasyPayReceiptTriplePlusProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(extasyPayReceiptTriplePlusProperties.url()).build();
    }

    @Bean
    public RestClient extasyPayQRRestClient(ExtasyPayQRProperties extasyPayQRProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(extasyPayQRProperties.url()).build();
    }

    @Bean
    public RestClient pwPayRestClient(PwPayProperties pwPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(pwPayProperties.url()).build();
    }

    @Bean
    public RestClient honeyMoneyRestClient(HoneyMoneyProperties honeyMoneyProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(honeyMoneyProperties.url()).build();
    }

    @Bean
    public RestClient payscrowRestClient(PayscrowPropertiesImpl payscrowProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payscrowProperties.url()).build();
    }

    @Bean
    public RestClient payscrowHighCheckRestClient(PayscrowHighCheckProperties payscrowHighCheckProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payscrowHighCheckProperties.url()).build();
    }

    @Bean
    public RestClient payscrowWTRestClient(PayscrowWTProperties payscrowWTProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payscrowWTProperties.url()).build();
    }

    @Bean
    public RestClient payscrowSimRestClient(PayscrowSimProperties payscrowSimProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payscrowSimProperties.url()).build();
    }

    @Bean
    public RestClient payscrowLowRestClient(PayscrowLowProperties payscrowLowProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payscrowLowProperties.url()).build();
    }

    @Bean
    public RestClient payscrowTransgranRestClient(PayscrowTransgranProperties payscrowTransgranProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payscrowTransgranProperties.url()).build();
    }


    @Bean
    public RestClient onlyPaysRestClient(OnlyPaysProperties onlyPaysProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(onlyPaysProperties.url()).build();
    }

    @Bean
    public RestClient payLeeRestClient(PayLeePropertiesImpl payLeeProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payLeeProperties.url()).build();
    }

    @Bean
    public RestClient stormTrade13RestClient(StormTrade13Properties stormTrade13Properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(stormTrade13Properties.url()).build();
    }

    @Bean
    public RestClient stormTradeRestClient(StormTradeProperties stormTradeProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(stormTradeProperties.url()).build();
    }

    @Bean
    public RestClient settleXRestClient(SettleXPropertiesImpl settleXProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(settleXProperties.url()).build();
    }

    @Bean
    public RestClient settleX15RestClient(SettleX15Properties settleX15Properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(settleX15Properties.url()).build();
    }

    @Bean
    public RestClient plataPaymentRestClient(PlataPaymentProperties properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(properties.url()).build();
    }

    @Bean
    public RestClient plata18RestClient(Plata18Properties properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(properties.url()).build();
    }

    @Bean
    public RestClient payLeeQRRestClient(PayLeeQRProperties payLeeProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(payLeeProperties.url()).build();
    }

    @Bean
    public RestClient yoloRestClient(YoloPropertiesImpl yoloProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(yoloProperties.url()).build();
    }

    @Bean
    public RestClient yoloSimRestClient(YoloSimProperties yoloSimProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(yoloSimProperties.url()).build();
    }

    @Bean
    public RestClient norosRestClient(NorosPropertiesImpl norosProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(norosProperties.url()).build();
    }

    @Bean
    public RestClient norosHighCheckRestClient(NorosHighCheckProperties norosHighCheckProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(norosHighCheckProperties.url()).build();
    }

    @Bean
    public RestClient fiatCutRestClient(FiatCutProperties fiatCutProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(fiatCutProperties.url()).build();
    }

    @Bean
    public RestClient cashOutRestClient(CashOutProperties cashOutProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(cashOutProperties.url()).build();
    }

    @Bean
    public RestClient goatxRestClient(GoatxPropertiesImpl goatxProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(goatxProperties.url()).build();
    }

    @Bean
    public RestClient goatxSimRestClient(GoatxSimProperties goatxSimProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(goatxSimProperties.url()).build();
    }

    @Bean
    public RestClient lotrienRestClient(LotrienProperties lotrienProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(lotrienProperties.url()).build();
    }

    @Bean
    public RestClient gambitRestClient(GambitImplProperties gambitProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(gambitProperties.url()).build();
    }

    @Bean
    public RestClient gambitSimRestClient(GambitSimProperties gambitSimProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(gambitSimProperties.url()).build();
    }

    @Bean
    public RestClient asgardRestClient(AsgardImplProperties asgardProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public RestClient asgardWTRestClient(AsgardWTProperties asgardProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public RestClient asgardSimRestClient(AsgardSimProperties asgardProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public RestClient asgardHighCheckRestClient(AsgardHighCheckProperties asgardProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(asgardProperties.url()).build();
    }

    @Bean
    public RestClient deoraRestClient(DeoraProperties deoraProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public RestClient deoraSimRestClient(DeoraSimProperties deoraProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public RestClient deoraLowCheckRestClient(DeoraLowCheckProperties deoraProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public RestClient deoraPdfRestClient(DeoraPdfProperties deoraProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(deoraProperties.url()).build();
    }

    @Bean
    public RestClient meridianPayRestClient(MeridianPayProperties meridianPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public RestClient meridianPayHighCheckRestClient(MeridianPayHighCheckProperties meridianPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public RestClient meridianPayLowCheckRestClient(MeridianPayLowCheckProperties meridianPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public RestClient meridianPaySimRestClient(MeridianPaySimProperties meridianPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public RestClient meridianPayNspkRestClient(MeridianPayNspkProperties meridianPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(meridianPayProperties.url()).build();
    }

    @Bean
    public RestClient souzRestClient(SouzProperties souzProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public RestClient souzSbpQrRestClient(SouzSbpQrProperties souzProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public RestClient souzSimRestClient(SouzSimProperties souzProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public RestClient souzPdfRestClient(SouzPdfProperties souzProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(souzProperties.url()).build();
    }

    @Bean
    public RestClient base51RestClient(Base51Properties base51Properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public RestClient base51SimRestClient(Base51SimProperties base51Properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public RestClient base51HighCheckRestClient(Base51HighCheckProperties base51Properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public RestClient base51LowCheckRestClient(Base51LowCheckProperties base51Properties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(base51Properties.url()).build();
    }

    @Bean
    public RestClient bucksPaySimRestClient(BucksPaySimProperties bucksPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(bucksPayProperties.url()).build();
    }

    @Bean
    public RestClient bucksPayRestClient(BucksPayPropertiesImpl bucksPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(bucksPayProperties.url()).build();
    }

    @Bean
    public RestClient manyPayRestClient(ManyPayPropertiesImpl manyPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(manyPayProperties.url()).build();
    }

    @Bean
    public RestClient manyPayLowCheckRestClient(ManyPayLowCheckProperties manyPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(manyPayProperties.url()).build();
    }

    @Bean
    public RestClient manyPayHighCheckRestClient(ManyPayHighCheckProperties manyPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(manyPayProperties.url()).build();
    }

    @Bean
    public RestClient prismaPayRestClient(PrismaPayProperties prismaPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(prismaPayProperties.url()).build();
    }

    @Bean
    public RestClient paySyncRestClient(PaySyncProperties paySyncProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(paySyncProperties.url()).build();
    }

    @Bean
    public RestClient cubeRestClient(CubePropertiesImpl cubeProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(cubeProperties.url()).build();
    }

    @Bean
    public RestClient rsPayRestClient(RSPayImplProperties rsPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(rsPayProperties.url()).build();
    }

    @Bean
    public RestClient watRestClient(WatPropertiesImpl watProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(watProperties.url()).build();
    }

    @Bean
    public RestClient tronExRestClient(TronExSimProperties tronExProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(tronExProperties.url()).build();
    }

    @Bean
    public RestClient primeWalletRestClient(PrimeWalletProperties primeWalletProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(primeWalletProperties.url()).build();
    }

    @Bean
    public RestClient bayPayRestClient(BayPayImplProperties bayPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(bayPayProperties.url()).build();
    }

    @Bean
    public RestClient hellBitRestClient(HellBitProperties hellBitProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(hellBitProperties.url()).build();
    }

    @Bean
    public RestClient hesoyamRestClient(HesoyamImplProperties hesoyamProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(hesoyamProperties.url()).build();
    }

    @Bean
    public RestClient smackPayRestClient(SmackPayProperties smackPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(smackPayProperties.url()).build();
    }

    @Bean
    public RestClient viatrumRestClient(ViatrumProperties viatrumProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(viatrumProperties.url()).build();
    }

    @Bean
    public RestClient bitBolideRestClient(BitBolideProperties bitBolideProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(bitBolideProperties.url()).build();
    }
}
