package com.myplaylists.web.posts.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;

@Data
public class CommentAddForm {
    @NotBlank
    @Length(min=1,max=50)
    private String content;
}
