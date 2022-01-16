package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import com.myplaylists.web.posts.form.PostsForm;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Posts {
    @Id @GeneratedValue
    @Column(name ="posts_id")
    private Long id;

    private String title;

    @OneToOne
    @JoinColumn(name = "member_id")
    private Member postsOwner;

    private String description;

    private Long likes = 0L;

    private Long favorite = 0L;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    @OneToMany(mappedBy = "posts",cascade = CascadeType.ALL)
    @JsonBackReference
    List<Playlist> playlists = new ArrayList<>();

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

    private void addPlaylist(Playlist playlist) {
        this.playlists.add(playlist);
        playlist.setPosts(this);
    }

    private void setPostsOwner(Member postsOwner){
        this.postsOwner = postsOwner;
        postsOwner.getPosts().add(this);
    }
}
