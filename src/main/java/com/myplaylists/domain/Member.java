package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter
@AllArgsConstructor @NoArgsConstructor
@SequenceGenerator(name = "ENTITY_ID_GENERATOR"
        ,sequenceName = "hibernate_sequence"
        ,allocationSize = 50)
public class Member {
    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE, generator="ENTITY_ID_GENERATOR")
    @Column(name = "member_id")
    private Long id;

    private String nickname;

    private String email;

    private String password;

    private String token;

    private LocalDateTime joinedAt;

    private LocalDateTime tokenGeneratedAt;

    private boolean emailVerified = false;

    private String introduce;

    @OneToMany(mappedBy = "postsOwner")
    @JsonBackReference
    private List<Posts> posts = new ArrayList<>();

    public void generateToken() {
        this.token = UUID.randomUUID().toString();
        this.tokenGeneratedAt = LocalDateTime.now();
    }

    public void checkEmailVerified() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }
}
