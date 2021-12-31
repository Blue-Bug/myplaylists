package com.myplaylists.web.setting.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class PasswordForm {

    @Length(min=8,max=20)
    @NotBlank
    private String password;

    @Length(min=8,max=20)
    @NotBlank
    private String passwordConfirm;
}
