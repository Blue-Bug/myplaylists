package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.fasterxml.jackson.annotation.JsonManagedReference;
import com.myplaylists.web.posts.form.PostsForm;
import lombok.*;
import org.hibernate.annotations.*;

import javax.persistence.*;
import javax.persistence.CascadeType;
import javax.persistence.Entity;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "ENTITY_ID_GENERATOR"
        ,sequenceName = "hibernate_sequence"
        ,allocationSize = 50)
public class Posts {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="ENTITY_ID_GENERATOR")
    @Column(name ="posts_id")
    private Long id;

    private String title;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference
    private Member postsOwner;

    private String description;

    private Long likes = 0L;

    private Long favorite = 0L;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "posts"
        ,cascade = CascadeType.ALL
        ,orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference
    private List<Playlist> playlists = new ArrayList<>();

    @OneToMany(mappedBy = "commentedPosts"
            ,cascade = CascadeType.ALL
            ,orphanRemoval = true)
    @OnDelete(action = OnDeleteAction.CASCADE)
    @JsonManagedReference
    private List<Comment> comments = new ArrayList<>();

    //생성 메서드
    public static Posts createPosts(Member postsOwner, String title, String description,List<Playlist> playlists) {
        Posts posts = new Posts();
        posts.setPostsOwner(postsOwner);
        posts.setTitle(title);
        posts.setDescription(description);
        posts.setCreatedAt(LocalDateTime.now());
        posts.setModifiedAt(LocalDateTime.now());
        
        for(Playlist playlist : playlists){
            posts.addPlaylist(playlist);
        }

        return posts;
    }

    public void addPlaylist(Playlist playlist) {
        this.playlists.add(playlist);
        playlist.setPosts(this);
    }

    private void setPostsOwner(Member postsOwner){
        this.postsOwner = postsOwner;
        postsOwner.getPosts().add(this);
    }
}
