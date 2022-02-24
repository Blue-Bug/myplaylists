package com.myplaylists.web.posts.form;

import com.myplaylists.web.posts.validator.NotEmptyElement;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.List;

@Data
public class PlaylistsEditForm {
    @NotNull
    private Long id;

    @NotBlank
    private String playlistType;

    @NotBlank
    @Length(min=1,max=20)
    private String title;

    @Length(max=30)
    private String description;

    @NotEmptyElement
    @Size(min=1,max=30)
    private List<@NotBlank @Length(min=1,max=50) String> links;
}
