package net.rcetech.md.integrations.config;

import net.rcetech.md.integrations.properties.*;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.http.converter.StringHttpMessageConverter;
import org.springframework.web.client.RestClient;

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
    public RestClient geoTransferRestClient(GeoTransferProperties geoTransferProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(geoTransferProperties.url()).build();
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
    public RestClient evoPayRestClient(EvoPayProperties evoPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(evoPayProperties.url()).build();
    }






    @Bean
    public RestClient honeyMoneyRestClient(HoneyMoneyProperties honeyMoneyProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(honeyMoneyProperties.url()).build();
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
    public RestClient fiatCutRestClient(FiatCutProperties fiatCutProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(fiatCutProperties.url()).build();
    }

    @Bean
    public RestClient cashOutRestClient(CashOutProperties cashOutProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(cashOutProperties.url()).build();
    }



    @Bean
    public RestClient lotrienRestClient(LotrienPropertiesImpl lotrienProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(lotrienProperties.url()).build();
    }


    @Bean
    public RestClient gambitSimRestClient(GambitSimProperties gambitSimProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(gambitSimProperties.url()).build();
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

    @Bean
    public RestClient canPayRestClient(CanPayProperties canPayProperties) {
        return get30SecondsReadTimeoutRestClientBuilder().baseUrl(canPayProperties.url()).build();
    }
}
