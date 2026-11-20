package com.book.store.online_book_store.Service;

import com.book.store.online_book_store.DTO.PendingRegistration;
import com.book.store.online_book_store.DTO.Request.RegistrationRequest;
import com.book.store.online_book_store.DTO.Response.ApiResponse;
import com.book.store.online_book_store.DTO.Response.RegistrationResponse;
import com.book.store.online_book_store.Exception.AlreadyRegisteredException;
import com.book.store.online_book_store.Exception.RegistrationAlreadyPendingException;
import com.book.store.online_book_store.Repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.time.Instant;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

@RequiredArgsConstructor
@Service
public class RegistrationService {
    private final UserRepository userRepository;
    //private final PasswordEncoder passwordEncoder;
    private final PendingRegistrationService pendingRegistrationService;
    private final OtpService otpService;
    @Value("${otp.pendingAccountTtl}")
    private Duration pendingRegistrationTtl;
    @Value("${otp.otpGapCooldown}")
    private Duration otpGapCooldown;

    public RegistrationResponse register(RegistrationRequest request) {

        String name = request.getName().trim().toLowerCase(Locale.ROOT);
        String email = request.getEmail().trim().toLowerCase(Locale.ROOT);
        String passwordHashed = request.getPassword();
        //String passwordHashed = passwordEncoder.encode(request.getPassword());

        PendingRegistration pendingRegistration = new PendingRegistration(
                name,
                email,
                passwordHashed
        );

        // Check if the user exists in database
        if (userRepository.existsByEmail(email)) {
            throw new AlreadyRegisteredException("Already Registered");
        }

        if (pendingRegistrationService.existsByEmail(email)) {
            // return with registration pending
            UUID registrationId = pendingRegistrationService.getUuid(email);
            throw new RegistrationAlreadyPendingException(registrationId.toString(), "Pending Registration Already exists");
        }
        // store user in cache user temp table
        String registrationId = pendingRegistrationService.save(pendingRegistration, pendingRegistrationTtl);
        // Call SendOtp
        otpService.sendOtp(email);
        //regId, pendingSessionExpiresAt, otpExpiresAt, otpAttempts     *needed*
        Instant sessionExpiresAt = Instant.now().plus(pendingRegistrationTtl);
        Instant resendAvailableAt = Instant.now().plus(otpGapCooldown);
        //otpTtl, 60 seconds resend cooldown    email or UUID:cooldown -> cooldown TTL   using the UUId to determine when can the user request a resend
        //otpAttempts need save in cache with otp -> Attempts (5m ttl) as attempt (return from OtpService.generateOtp())

        // address -> attemptsPerAddress TTL 24h
            //otpTtl return with the otpAttempts
        return new RegistrationResponse(registrationId, sessionExpiresAt, resendAvailableAt);
    }

    public String resend(){
        return "we are in resend";
    }
}
