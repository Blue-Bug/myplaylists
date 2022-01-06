package com.myplaylists.domain;

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

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name="playlist_id")
    private Playlist playlist;

    private String link;

    public static Link createLink(String link){
        Link newLink = new Link();
        newLink.setLink(link);
        return newLink;
    }
}

