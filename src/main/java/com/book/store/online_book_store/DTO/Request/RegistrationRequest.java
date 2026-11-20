package com.book.store.online_book_store.DTO.Request;

import com.book.store.online_book_store.Validation.PasswordMatches;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

@AllArgsConstructor
@Getter
@Setter
@PasswordMatches
public class RegistrationRequest {
    @NotBlank
    private String name;
    @Email
    @NotBlank
    // check if email exists or not
    private String email;
    @Size(min = 8, message = "Password must be at least 8 characters")
    @Pattern(regexp = ".*[a-zA-Z].*", message = "Password must contain at least one letter")
    @Pattern(regexp = ".*\\d.*", message = "Password must contain at least a number")
    @NotBlank
    private String password;
    @NotBlank
    private String confirmPassword;
    private String contribute;

}
