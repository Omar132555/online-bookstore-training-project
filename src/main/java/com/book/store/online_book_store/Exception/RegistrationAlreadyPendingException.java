package com.book.store.online_book_store.Exception;

import lombok.Getter;

@Getter
public class RegistrationAlreadyPendingException extends RuntimeException {
    private final String registrationId;
;    public RegistrationAlreadyPendingException(String registrationId, String message) {
        super(message);
        this.registrationId = registrationId;
    }
}
