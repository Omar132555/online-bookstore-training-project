package com.book.store.online_book_store.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.Instant;

@Getter
@AllArgsConstructor
public class ResendOtpResponse {
    Long remainingAttempts;
    Instant expiresAt;
    Instant resendAvailableAt;
}
