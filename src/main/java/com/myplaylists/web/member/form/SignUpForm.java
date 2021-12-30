package com.myplaylists.web.member.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.Email;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class SignUpForm {

    @Length(min=2,max=20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
    @NotBlank
    private String nickname;

    @Email
    @NotBlank
    private String email;

    @Length(min=8,max=20)
    @NotBlank
    private String password;

    @Length(min=8,max=20)
    @NotBlank
    private String passwordConfirm;
}
