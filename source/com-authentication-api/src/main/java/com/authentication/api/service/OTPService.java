package com.authentication.api.service;

import com.authentication.api.service.redis.RedisService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

@Service
@Slf4j
public class OTPService {
    private final SecureRandom secureRandom;

    private List<Integer> numberRand = new ArrayList<>();

    public static final int FIVE_MINUTES = 5 * 60; // 5 minutes

    public static final int TEN_MINUTES = 10 * 60; // 10 minutes

    @Autowired
    private RedisService redisService;


    public OTPService() throws NoSuchAlgorithmException {
        secureRandom = SecureRandom.getInstance("SHA1PRNG");
        for (int i = 0; i < 10; i++) {
            numberRand.add(i);
        }
    }

    public synchronized String generate(int maxLength) {
        final StringBuilder otp = new StringBuilder(maxLength);
        for (int i = 0; i < maxLength; i++) {
            otp.append(secureRandom.nextInt(9));
        }
        return otp.toString();
    }

    public void storeOtp(String email, String otp) {
        String key = redisService.buildKey(email);
        redisService.put(key, otp, FIVE_MINUTES);
    }

    public boolean verifyOtp(String email, String otp) {
        String key = redisService.buildKey(email);
        String cachedOtp = redisService.get(key, String.class);
        return Objects.equals(otp, cachedOtp);
    }

    public void deleteOtp(String email) {
        String key = redisService.buildKey(email);
        redisService.deleteByPrefix(key);
    }

    public synchronized String resendOtp(String email) {
        String key = redisService.buildKey(email, "resend");
        String current = redisService.get(key, String.class);

        int resendCount;
        try {
            resendCount = current != null ? Integer.parseInt(current) : 0;
        } catch (NumberFormatException e) {
            resendCount = 0;
        }

        if (resendCount >= 3) {
            return null;
        }

        String otp = generate(6);
        this.storeOtp(email, otp);

        this.redisService.put(key, resendCount + 1, TEN_MINUTES);

        return otp;
    }
}
