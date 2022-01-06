package com.myplaylists.web.posts;

import com.myplaylists.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.transaction.annotation.Transactional;

@Transactional(readOnly = true)
public interface PostsRepository extends JpaRepository<Posts,Long> {
}
