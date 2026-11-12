package tgb.cryptoexchange.merchantdetails.service;

import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class SleepingService {

    public void sleep(int seconds) throws InterruptedException {
        TimeUnit.SECONDS.sleep(seconds);
    }
}
