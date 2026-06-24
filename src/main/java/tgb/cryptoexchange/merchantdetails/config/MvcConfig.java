package tgb.cryptoexchange.merchantdetails.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.File;

import static tgb.cryptoexchange.merchantdetails.service.ReceiptService.RECEIPT_FOLDER;

@Configuration
public class MvcConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String jarPath = System.getProperty("user.dir");
        String baseReceiptsDir = jarPath + File.separator + RECEIPT_FOLDER + File.separator;
        registry.addResourceHandler("/merchant-details/" + RECEIPT_FOLDER + "/**")
                .addResourceLocations("file:" + baseReceiptsDir);
    }

}
