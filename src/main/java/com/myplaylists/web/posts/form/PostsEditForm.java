package com.myplaylists.web.posts.form;

import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class PostsEditForm {
    @NotBlank
    @Length(min=1,max=30)
    private String title;

    @Length(max=80)
    private String description;

    @Valid
    @Size(min=1,max=10)
    List<PlaylistsEditForm> playlistsEditForms;
}
