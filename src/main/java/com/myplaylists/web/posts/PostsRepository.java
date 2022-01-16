package com.myplaylists.web.posts;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PostsRepository extends JpaRepository<Posts,Long> {
    Optional<List<Posts>> findByPostsOwner(Member profileMember);
}
