package com.myplaylists.web.posts.form;

import com.myplaylists.domain.Playlist;
import com.myplaylists.web.posts.validator.NotEmptyElement;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostsForm {
    @NotBlank
    @Length(min=1,max=30)
    private String title;

    @Length(max=80)
    private String description;

    @Valid
    @NotEmpty
    @Size(min=1,max=10)
    private List<PlaylistsForm> playlistsForms;
}
