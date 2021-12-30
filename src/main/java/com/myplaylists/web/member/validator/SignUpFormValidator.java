package com.myplaylists.web.member.validator;

import com.myplaylists.web.member.MemberRepository;
import com.myplaylists.web.member.form.SignUpForm;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.validation.Errors;
import org.springframework.validation.Validator;

@Component
@RequiredArgsConstructor
public class SignUpFormValidator implements Validator {

    private final MemberRepository memberRepository;

    @Override
    public boolean supports(Class<?> clazz) {
        return clazz.isAssignableFrom(SignUpForm.class);
    }

    @Override
    public void validate(Object target, Errors errors) {
        SignUpForm signUpForm = (SignUpForm) target;

        if(memberRepository.existsByEmail(signUpForm.getEmail())){
            errors.rejectValue("email","invalid.email","이미 존재하는 이메일입니다.");
        }

        if(memberRepository.existsByNickname(signUpForm.getNickname())){
            errors.rejectValue("nickname","invalid.nickname","이미 존재하는 닉네임입니다.");
        }

        if(!signUpForm.getPassword().equals(signUpForm.getPasswordConfirm())){
            errors.rejectValue("passwordConfirm","invalid.password","비밀번호와 일치하지 않습니다.");
        }
    }
}
