package com.myplaylists.web.posts.form;

import com.myplaylists.domain.Playlist;
import com.myplaylists.web.posts.validator.NotEmptyElement;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.Valid;
import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import java.util.ArrayList;
import java.util.List;

@Data
public class PostsForm {
    @NotBlank
    @Length(min=1,max=20)
    private String title;

    @Length(max=50)
    private String description;

    @Valid
    @NotEmpty
    private List<PlaylistsForm> playlistsForms;
}
