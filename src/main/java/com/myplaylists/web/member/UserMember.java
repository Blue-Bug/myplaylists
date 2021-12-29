package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import lombok.Getter;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;

import java.util.Collection;
import java.util.List;

@Getter
public class UserMember extends User {
    private Member member;

    public UserMember(Member member) {
        super(member.getNickname(), member.getPassword(), List.of(new SimpleGrantedAuthority("ROLE_USER")));
        this.member = member;
    }
}
