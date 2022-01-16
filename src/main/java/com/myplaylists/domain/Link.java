package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.myplaylists.domain.Playlist;
import lombok.*;
import org.springframework.security.core.parameters.P;

import javax.persistence.*;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Link {
    @Id @GeneratedValue
    private Long id;

    @OneToOne
    @JoinColumn(name="playlist_id")
    @JsonBackReference
    private Playlist playlist;

    private String link;

    public static Link createLink(String link){
        Link newLink = new Link();
        newLink.setLink(link);
        return newLink;
    }
}

