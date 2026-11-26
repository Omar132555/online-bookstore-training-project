package com.book.store.online_book_store.Exception;


public class ResendCooldownException extends RuntimeException {
    private final Long remainingTtl;
    public ResendCooldownException(Long remainingTtl) {
        super("Please wait " + remainingTtl + " seconds before requesting a new code");
        this.remainingTtl = remainingTtl;
    }

    public Long getRemainingTtl() {
        return remainingTtl;
    }
}
