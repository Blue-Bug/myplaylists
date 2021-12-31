package com.myplaylists.web.setting.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

@Data
public class ProfileForm {
    @Length(max=20)
    private String introduce;
}
