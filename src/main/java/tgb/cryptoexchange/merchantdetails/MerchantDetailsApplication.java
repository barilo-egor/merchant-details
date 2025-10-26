package tgb.cryptoexchange.merchantdetails;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;

@SpringBootApplication
@ConfigurationPropertiesScan("tgb.cryptoexchange.merchantdetails.properties")
public class MerchantDetailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantDetailsApplication.class, args);
    }

}
