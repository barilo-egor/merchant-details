package net.rcetech.md;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class MerchantDetailsApplication {

    public static void main(String[] args) {
        SpringApplication.run(MerchantDetailsApplication.class, args);
    }

}
