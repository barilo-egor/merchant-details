package net.rcetech.md.app.service;

import org.springframework.stereotype.Service;

@Service
public class SleepService {

    public void sleep(Long ms) {
        try {
            Thread.sleep(ms);
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }
    }
}
