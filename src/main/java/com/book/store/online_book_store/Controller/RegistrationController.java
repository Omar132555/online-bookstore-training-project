package com.book.store.online_book_store.Controller;

import com.book.store.online_book_store.DTO.Request.RegistrationRequest;
import com.book.store.online_book_store.DTO.Response.ApiResponse;
import com.book.store.online_book_store.DTO.Response.RegistrationResponse;
import com.book.store.online_book_store.DTO.Response.ResendOtpResponse;
import com.book.store.online_book_store.Service.RegistrationService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/registration")
@RequiredArgsConstructor
public class RegistrationController {
    private final RegistrationService registrationService;
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<RegistrationResponse>> register(@Valid @RequestBody RegistrationRequest request){
        RegistrationResponse response = registrationService.register(request);

        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response));
    }

    @PostMapping("/{id}/resend")
    public ResponseEntity<ApiResponse<ResendOtpResponse>> resend(@PathVariable String id){
        ResendOtpResponse response = registrationService.resend(id);
        return ResponseEntity.status(HttpStatus.ACCEPTED).body(ApiResponse.success(response));
    }

    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<String>> verify(@PathVariable String id){
        String response = registrationService.verify(id);
        return ResponseEntity.status(HttpStatus.OK).body(ApiResponse.success("We are in Verify Endpoint and this is your id -> " + response));
    }
}
