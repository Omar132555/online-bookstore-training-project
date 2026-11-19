package com.book.store.online_book_store.Validation;

import com.book.store.online_book_store.DTO.Request.RegistrationRequest;
import jakarta.validation.ConstraintValidator;
import jakarta.validation.ConstraintValidatorContext;

public class PasswordMatchesValidator implements ConstraintValidator<PasswordMatches, RegistrationRequest> {
    @Override
    public boolean isValid(RegistrationRequest request, ConstraintValidatorContext context){
        if (request.getPassword() == null ||
                request.getConfirmPassword() == null) {
            return true;
        }
        return request.getPassword().equals(request.getConfirmPassword());
    }
}
