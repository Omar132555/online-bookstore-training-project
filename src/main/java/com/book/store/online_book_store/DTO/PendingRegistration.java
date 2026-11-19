package com.book.store.online_book_store.DTO;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
public class PendingRegistration {
    private String name;
    private String email;
    private String passwordHashed;
}
