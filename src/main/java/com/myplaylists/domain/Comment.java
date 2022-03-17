package com.myplaylists.domain;

import com.fasterxml.jackson.annotation.JsonBackReference;
import lombok.*;

import javax.persistence.*;
import java.time.LocalDateTime;

@Entity @EqualsAndHashCode(of = "id")
@Getter @Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@SequenceGenerator(name = "ENTITY_ID_GENERATOR"
        ,sequenceName = "hibernate_sequence"
        ,allocationSize = 50)
public class Comment {

    @Id @GeneratedValue(strategy = GenerationType.SEQUENCE,generator = "ENTITY_ID_GENERATOR")
    @Column(name = "comment_id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "posts_id")
    @JsonBackReference
    private Posts commentedPosts;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    @JsonBackReference
    private Member writtenMember;

    private String nickname;

    private LocalDateTime createdAt;

    private LocalDateTime modifiedAt;

    private String content;

    public static Comment createComment(Member member,Posts posts,String content){
        Comment comment = new Comment();
        comment.setContent(content);
        comment.setCreatedAt(LocalDateTime.now());
        comment.setModifiedAt(LocalDateTime.now());
        comment.setNickname(member.getNickname());

        comment.setCommentedPosts(posts);
        comment.setWrittenMember(member);
        return comment;
    }

    private void setCommentedPosts(Posts posts){
        this.commentedPosts = posts;
        posts.getComments().add(this);
    }

    private void setWrittenMember(Member member){
        this.writtenMember = member;
        member.getComments().add(this);
    }
}
