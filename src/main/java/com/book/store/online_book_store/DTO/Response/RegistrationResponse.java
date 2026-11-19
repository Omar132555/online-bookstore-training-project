package com.book.store.online_book_store.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.time.Duration;
import java.time.Instant;

@AllArgsConstructor
@Getter
@Setter
public class RegistrationResponse {
    String registrationId;
    Instant sessionExpiresAt;
    Instant resendAvailableAt;
}
