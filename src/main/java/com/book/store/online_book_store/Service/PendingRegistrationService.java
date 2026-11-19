package com.book.store.online_book_store.Service;

import com.book.store.online_book_store.DTO.PendingRegistration;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PendingRegistrationService {
    private final StringRedisTemplate redisTemplate;
    private static final String REGISTRATION_EMAIL_KEY = "registration:email:";
    private static final String UUID_KEY = "UUID:";
    private final ObjectMapper objectMapper;

    public PendingRegistration get(UUID registrationId){
        String key = UUID_KEY + registrationId;
        String pendingRegistrationString = redisTemplate.opsForValue().get(key);
        if(pendingRegistrationString == null){
            return null;
        }
        return objectMapper.readValue(pendingRegistrationString, PendingRegistration.class);
    }

    public UUID getUuid(String email)
    {
        String getRegistrationUuidKey = REGISTRATION_EMAIL_KEY + email;
        String value = redisTemplate.opsForValue().get(getRegistrationUuidKey);
        if (value == null) {
            return null;
        }
        return UUID.fromString(value);
    }

    /**
     * Saves the User data in cache and Returns the registration UUID associated with the email.
     */
    public String save(PendingRegistration registration, Duration pendingRegistrationTtl)
    {
        //save email -> value(userData)
        //save email -> UUID as a value to easily retrieve session
        UUID registrationId = UUID.randomUUID();
        String registrationKey = REGISTRATION_EMAIL_KEY + registration.getEmail();
        String userDataValue = objectMapper.writeValueAsString(registration);
        String UuidKey = UUID_KEY + registrationId.toString();
        redisTemplate.opsForValue().set(registrationKey, registrationId.toString(), pendingRegistrationTtl);
        redisTemplate.opsForValue().set(UuidKey, userDataValue, pendingRegistrationTtl);
        return registrationId.toString();
    }

    public boolean existsByEmail(String email){
        String key = REGISTRATION_EMAIL_KEY + email;
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }
}
