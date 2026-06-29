package com.authentication.api.scheduler;

import com.authentication.api.repository.AccountRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.Date;

@Component
@Slf4j
public class UserScheduler {
    @Autowired
    private AccountRepository accountRepository;

    @Scheduled(cron = "0 0 0 * * *", zone = "UTC")
    public void deleteUserPendingBefore1Days() {
        log.warn("======> Start scheduler deleteUserPendingBefore1Days user");
        Date date = Date.from(Instant.now().minus(1, ChronoUnit.DAYS));
        accountRepository.deleteUserPendingBeforeDate(date);
        log.warn("======> End scheduler deleteUserPendingBefore1Days user");
    }
}
