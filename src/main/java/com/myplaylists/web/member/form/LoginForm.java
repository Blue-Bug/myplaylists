package com.myplaylists.web.member.form;

import lombok.Builder;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.Pattern;

@Data
public class LoginForm {
    @Length(min=2,max=20)
    @Pattern(regexp = "^[ㄱ-ㅎ가-힣a-z0-9_-]{3,20}$")
    @NotBlank
    private String nickname;

    @Length(min=8,max=20)
    @NotBlank
    private String password;

    public LoginForm(String nickname, String password) {
        this.nickname = nickname;
        this.password = password;
    }
}
