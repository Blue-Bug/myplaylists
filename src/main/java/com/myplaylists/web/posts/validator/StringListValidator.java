package com.myplaylists.web.posts.validator;

import com.myplaylists.web.posts.form.PlaylistsForm;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

import javax.validation.ConstraintValidator;
import javax.validation.ConstraintValidatorContext;
import java.util.List;

@Component
public class StringListValidator implements ConstraintValidator<NotEmptyElement, List<String>> {

    @Override
    public void initialize(NotEmptyElement constraintAnnotation) {
        ConstraintValidator.super.initialize(constraintAnnotation);
    }

    @Override
    public boolean isValid(List<String> value, ConstraintValidatorContext context) {
        if(value == null) return false;
        return value.stream().allMatch(e -> e != null && !e.trim().isBlank());
    }
}

