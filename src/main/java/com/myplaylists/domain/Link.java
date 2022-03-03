package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.myplaylists.domain.Playlist;
import lombok.*;
import org.springframework.security.core.parameters.P;

import javax.persistence.*;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "ENTITY_ID_GENERATOR"
        ,sequenceName = "hibernate_sequence"
        ,allocationSize = 50)
public class Link {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="ENTITY_ID_GENERATOR")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="playlist_id")
    @JsonBackReference
    private Playlist playlist;

    private String link;

    public static Link createLink(String link){
        Link newLink = new Link();
        newLink.setLink(link);
        return newLink;
    }
    public static Link createLink(String link,Playlist playlist){
        Link newLink = new Link();
        newLink.setLink(link);
        newLink.setPlaylist(playlist);
        return newLink;
    }
}

