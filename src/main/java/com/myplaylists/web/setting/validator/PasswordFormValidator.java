package com.myplaylists.web.setting.validator;

import com.myplaylists.web.setting.form.PasswordForm;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class PasswordFormValidator implements Validator {

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(PasswordForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        PasswordForm passwordForm = (PasswordForm) target;
        if(!passwordForm.getPassword().equals(passwordForm.getPasswordConfirm())){
            errors.rejectValue("password","invalid.password","입력한 비밀번호가 일치하지 않습니다.");
        }
    }
}
