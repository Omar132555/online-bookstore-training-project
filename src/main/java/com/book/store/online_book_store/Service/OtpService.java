package com.book.store.online_book_store.Service;

import com.book.store.online_book_store.DTO.PendingRegistration;
import com.book.store.online_book_store.DTO.Response.ResendOtpResponse;
import com.book.store.online_book_store.Exception.ResendCooldownException;
import com.book.store.online_book_store.Exception.SessionExpiredException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.Instant;
import java.util.Locale;
import java.util.UUID;
import java.util.concurrent.TimeUnit;


@Service
@RequiredArgsConstructor
public class OtpService {

    private final SecureRandom random;
    private final StringRedisTemplate redisTemplate;
    private final EmailService emailService;
    private final ObjectMapper objectMapper;
    private static final String RESEND_KEY = "resend:email:";
    private static final String OTP_COOLDOWN_KEY = "otp:UUID:cooldown:";
    private static final String OTP_ATTEMPTS_KEY = "otp:UUID:attempts:";
    private static final String USER_DATA_KEY = "registration:UUID:user-Data:";
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
    private String html = """
            <!DOCTYPE html>
            <html>
            <head>
                <meta charset="UTF-8">
                <meta name="viewport" content="width=device-width, initial-scale=1.0">
            </head>
            <body style="margin:0; padding:0; background-color:#f1f5f9; font-family:Arial, Helvetica, sans-serif;">
            
              <table role="presentation" width="100%" cellspacing="0" cellpadding="0" style="background-color:#f1f5f9; padding:40px 15px;">
                <tr>
                  <td align="center">
            
                    <table role="presentation" cellspacing="0" cellpadding="0" style="max-width:480px; width:100%; background:#ffffff; border-radius:14px; overflow:hidden; box-shadow:0 4px 20px rgba(0,0,0,0.08);">
            
                      <!-- Header -->
                      <tr>
                        <td align="center" style="background:linear-gradient(135deg,#2563eb,#1d4ed8); background-color:#2563eb; padding:30px 20px;">
                          <div style="font-size:40px; line-height:1;">&#9993;</div>
                          <h2 style="margin:12px 0 0; color:#ffffff; font-size:22px; font-weight:600;">
                            Verify Your Email
                          </h2>
                        </td>
                      </tr>
            
                      <!-- Body -->
                      <tr>
                        <td align="center" style="padding:35px 30px 25px;">
                          <p style="margin:0 0 20px; color:#475569; font-size:15px; line-height:1.6;">
                            Use the code below to complete your verification:
                          </p>
            
                          <div style="display:inline-block; background:#eff6ff; border:2px dashed #93c5fd; border-radius:12px; padding:16px 32px;">
                            <span style="font-size:34px; font-weight:bold; letter-spacing:10px; color:#2563eb; font-family:'Courier New', monospace;">
                              {{OTP}}
                            </span>
                          </div>
            
                          <p style="margin:25px 0 0; color:#64748b; font-size:14px;">
                            &#9200; This code expires in <strong>5 minutes</strong>.
                          </p>
                        </td>
                      </tr>
            
                      <!-- Footer -->
                      <tr>
                        <td align="center" style="background:#f8fafc; padding:20px 30px; border-top:1px solid #e2e8f0;">
                          <p style="margin:0; color:#94a3b8; font-size:12px; line-height:1.6;">
                            If you didn't request this code, you can safely ignore this email.
                          </p>
                        </td>
                      </tr>
            
                    </table>
            
                  </td>
                </tr>
              </table>
            
            </body>
            </html>
            """;

    public void sendOtp(String userEmail, String registrationId) {
        // check if the cooldown time expired to allow the user request ( not critical )
        // use the smtp and user email to send the otp
        // create the redis key value pair for otp cooldown & attempt
        String otp = generateOTP();

        String finalHtml = html.replace("{{OTP}}", otp);
        String resendKey = RESEND_KEY + userEmail;
        String otpCooldownKey = OTP_COOLDOWN_KEY + registrationId;
        String otpAttemptsKey = OTP_ATTEMPTS_KEY + registrationId;
        emailService.sendHtmlEmail(userEmail, "OTP Verification", finalHtml);
        redisTemplate.opsForValue().set(otpCooldownKey, "1", otpGapCooldown);    // code cooldown ( resend availability )
        redisTemplate.opsForValue().set(otpAttemptsKey, attemptsPerCode, otpTtl); // code attempts
        redisTemplate.opsForValue().set(resendKey, resendAttempts, pendingAccountTtl); // resend
    }

    public ResendOtpResponse resendOtp(UUID uuid) {
        String otpAttemptsKey = OTP_ATTEMPTS_KEY + uuid;
        String userDataKey = USER_DATA_KEY + uuid;
        String otpCooldownKey = OTP_COOLDOWN_KEY + uuid;
        String resendAttemptsKey = RESEND_KEY + uuid;

        String userDataValue = redisTemplate.opsForValue().get(userDataKey);
        if (userDataValue == null) {
            throw new SessionExpiredException("Session expired");
        }

        Boolean allowed = redisTemplate.opsForValue()
                .setIfAbsent(otpCooldownKey, "1", otpGapCooldown);
        if (!Boolean.TRUE.equals(allowed)) {
            Long remainingTtl = redisTemplate.getExpire(otpCooldownKey, TimeUnit.SECONDS);
            throw new ResendCooldownException(remainingTtl == null ? 0 : remainingTtl);
        }

        String resendAttemptsValue = redisTemplate.opsForValue().get(resendAttemptsKey);
        if (resendAttemptsValue == null) {
            throw new SessionExpiredException("Session Expired");
        }
        int resendAttempts = Integer.parseInt(resendAttemptsValue);
        if (resendAttempts <= 0) {
            throw new RuntimeException("You exceeded resend limit");
        }

        PendingRegistration pendingRegistration = objectMapper.readValue(userDataValue, PendingRegistration.class);
        String otp = generateOTP();
        String finalHtml = html.replace("{{OTP}}", otp);
        emailService.sendHtmlEmail(pendingRegistration.getEmail(), "Resend Otp", finalHtml);
        Long attemptsRemaining = redisTemplate.opsForValue().decrement(resendAttemptsKey);
        redisTemplate.opsForValue().set(otpAttemptsKey, attemptsPerCode, otpTtl); // code attempts
        Instant resendAvailableAt = Instant.now().plus(otpGapCooldown);
        Instant expiresAt = Instant.now().plus(otpTtl);

        return new ResendOtpResponse(attemptsRemaining, expiresAt, resendAvailableAt);
    }

    // remaining attempts, cooldown (resendAvailableAt), TTL(expiresAt)
    private String generateOTP() {
        return String.format(Locale.ROOT, "%06d", random.nextInt(1_000_000));
    }
}


// resendAttemptsPerUUID:UUID -> resendAttempts
// UUID:OTP -> OTP