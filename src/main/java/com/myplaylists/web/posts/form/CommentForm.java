package com.myplaylists.web.posts.form;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class CommentForm {
    private String id;

    private String content;

    private String nickname;

    private String createdAt;

    private String modifiedAt;
}
