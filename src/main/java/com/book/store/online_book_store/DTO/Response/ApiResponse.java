package com.book.store.online_book_store.DTO.Response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@AllArgsConstructor
@Getter
public class ApiResponse<T> {

    private T result;
    private ErrorResponse error;

    public static <T> ApiResponse<T> success(T result){
        if(result == null){
            throw new IllegalArgumentException("Result cannot be null");
        }
        return new ApiResponse<>(result, null);
    }

    public static <T> ApiResponse<T> failure(ErrorResponse error){
        if(error == null){
            throw new IllegalArgumentException("Error cannot be null");
        }
        return new ApiResponse<>(null, error);
    }
}
