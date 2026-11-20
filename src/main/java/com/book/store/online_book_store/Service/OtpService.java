package com.book.store.online_book_store.Service;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.util.Locale;
import java.util.UUID;


@Service
@RequiredArgsConstructor
public class OtpService {

    private final SecureRandom random;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private static final String RESEND_KEY = "resend:email:";
    private static final String OTP_COOLDOWN_KEY = "otp:cooldown:";
    private static final String OTP_ATTEMPTS_KEY = "otp:attempts:";
    @Value("${otp.otpGapCooldown}")
    private Duration otpGapCooldown;
    @Value("${otp.attemptsPerCode}")
    private String attemptsPerCode;
    @Value("${otp.resendAttempts}")
    private String resendAttempts;
    @Value("${otp.otpTtl}")
    private Duration otpTtl;
    @Value("${otp.pendingAccountTtl}")
    private Duration pendingAccountTtl;

    public void sendOtp(String userEmail) {
        // check if the cooldown time expired to allow the user request ( not critical )
        // use the smtp and user email to send the otp
        // create the redis key value pair for otp cooldown & attempt
        String otp = String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
        String html = """
                <!DOCTYPE html>
                <html>
                <head>
                    <meta charset="UTF-8">
                </head>
                <body style="font-family: Arial; background: #f4f4f4; padding: 30px;">
                
                    <div style="max-width: 500px; margin: auto;
                                background: white; padding: 30px;
                                border-radius: 10px; text-align: center;">
                
                        <h2>Verify Your Email</h2>
                
                        <p>Your OTP code is:</p>
                
                        <h1 style="letter-spacing: 8px; color: #2563eb;">
                            %s
                        </h1>
                
                        <p>This code expires in 5 minutes.</p>
                
                    </div>
                
                </body>
                </html>
                """.formatted(otp);
        String resendKey = RESEND_KEY + userEmail;
        String otpCooldownKey = OTP_COOLDOWN_KEY + otp;
        String otpAttemptsKey = OTP_ATTEMPTS_KEY + otp;
        emailService.sendHtmlEmail(userEmail, "OTP Verification", html);
        redisTemplate.opsForValue().set(otpCooldownKey, userEmail, otpGapCooldown);    // code cooldown ( resend availability )
        redisTemplate.opsForValue().set(otpAttemptsKey, attemptsPerCode, otpTtl); // code attempts
        redisTemplate.opsForValue().set(resendKey, resendAttempts, pendingAccountTtl); // resend
    }

    public void resend(UUID uuid) {

    }
}
