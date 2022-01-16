package com.myplaylists.web.posts.form;

import com.myplaylists.domain.Playlist;
import com.myplaylists.web.posts.validator.NotEmptyElement;
import lombok.Data;
import org.hibernate.validator.constraints.Length;

import javax.validation.constraints.NotBlank;
import javax.validation.constraints.NotEmpty;
import javax.validation.constraints.NotNull;
import javax.validation.constraints.Size;
import java.util.ArrayList;
import java.util.List;

@Data
public class PlaylistsForm {
    @NotBlank
    private String playlistType;

    @NotBlank
    @Length(max=20)
    private String playlistTitle;

    @Length(max=30)
    private String playlistDescription;

    @NotEmptyElement(message = "비어 있는 링크가 있습니다.")
    @Size(min=1,max=30)
    private List<String> links;
}
