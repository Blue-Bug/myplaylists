package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.util.ArrayList;
import java.util.List;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "ENTITY_ID_GENERATOR"
        ,sequenceName = "hibernate_sequence"
        ,allocationSize = 50)
public class Playlist {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="ENTITY_ID_GENERATOR")
    @Column(name ="playlist_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    @JsonBackReference
    private Posts posts;

    private String playlistType;

    @OneToMany(mappedBy = "playlist"
            ,cascade = CascadeType.ALL
            ,orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference
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

    public void addLink(Link link) {
        this.links.add(link);
        link.setPlaylist(this);
    }
}
