package com.book.store.online_book_store.Exception;

import com.book.store.online_book_store.DTO.Response.ApiResponse;
import com.book.store.online_book_store.DTO.Response.ErrorResponse;
import com.book.store.online_book_store.Service.PendingRegistrationService;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

@RequiredArgsConstructor
@RestControllerAdvice
public class GlobalHandlerException {

    private final PendingRegistrationService pendingRegistrationService;

    @ExceptionHandler(EmailSendingException.class)
    public ResponseEntity<ApiResponse<Void>> handleEmailSendingException(
            EmailSendingException ex
    ) {
        ErrorResponse error = new ErrorResponse(1004, "Error during sending OTP mail");
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<ApiResponse<Void>> handleHttpMessageNotReadableException(HttpMessageNotReadableException exception) {
        ErrorResponse error = new ErrorResponse(
                1001,
                "Invalid request body"
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ApiResponse<Void>> handleMethodArgumentNotValidException(MethodArgumentNotValidException exception) {
        Map<String, String> errors = new HashMap<>();
        for (FieldError error : exception.getBindingResult().getFieldErrors()) {
            errors.put(error.getField(), error.getDefaultMessage());
        }
        ErrorResponse error = new ErrorResponse(1002, errors);
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(RegistrationAlreadyPendingException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyRegisteredException(RegistrationAlreadyPendingException ex) {
        Map<String, String> response = new HashMap<>();
        response.put("errorType", ex.getMessage());
        response.put("UUID", ex.getRegistrationId());
        ErrorResponse error = new ErrorResponse(2002, response);

        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(error));
    }

    @ExceptionHandler(AlreadyRegisteredException.class)
    public ResponseEntity<ApiResponse<Void>> handleAlreadyRegisteredException(AlreadyRegisteredException exception) {
        ErrorResponse error = new ErrorResponse(2003, exception.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(ApiResponse.failure(error));
    }
}
