package com.myplaylists.domain;

import lombok.*;
import org.hibernate.annotations.Fetch;
import org.hibernate.annotations.FetchMode;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Playlist {
    @Id @GeneratedValue
    @Column(name ="playlist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    private Posts posts;

    private String playlistType;

    @OneToMany(mappedBy = "playlist",cascade = CascadeType.ALL)
    @Fetch(FetchMode.SUBSELECT)
    private List<Link> links = new ArrayList<>();

    private String title;

    private String description;

    public static Playlist createPlaylist(String title,String description,String playlistType,List<Link> links){
        Playlist playlist = new Playlist();
        playlist.setTitle(title);
        playlist.setPlaylistType(playlistType);
        playlist.setDescription(description);
        for(Link link : links){
            playlist.addLink(link);
        }
        return playlist;
    }

    private void addLink(Link link) {
        this.links.add(link);
        link.setPlaylist(this);
    }
}
