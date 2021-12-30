package com.myplaylists.domain;

import lombok.*;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.Id;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity @EqualsAndHashCode(of="id")
@Getter @Setter @Builder
@AllArgsConstructor @NoArgsConstructor
public class Member {
    @Id @GeneratedValue
    private Long id;

    private String nickname;

    private String email;

    private String password;

    private String token;

    private LocalDateTime joinedAt;

    private LocalDateTime tokenGeneratedAt;

    private boolean emailVerified;

    public void generateToken() {
        this.token = UUID.randomUUID().toString();
        this.tokenGeneratedAt = LocalDateTime.now();
    }

    public void checkEmailVerified() {
        this.emailVerified = true;
        this.joinedAt = LocalDateTime.now();
    }
}
