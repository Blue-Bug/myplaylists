package com.myplaylists.web.member;

import com.myplaylists.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface MemberRepository extends JpaRepository<Member,Long> {
    boolean existsByEmail(String email);

    boolean existsByNickname(String nickname);

    Member findByNickname(String jun);
}
