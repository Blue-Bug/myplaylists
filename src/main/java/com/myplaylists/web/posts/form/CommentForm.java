package com.myplaylists.web.posts.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import java.time.LocalDateTime;

@Data
public class CommentForm {
    @NotBlank
    private String id;

    @NotBlank
    @Length(min=1,max=50)
    private String content;

    private String nickname;

    private String createdAt;

    private String modifiedAt;
}
