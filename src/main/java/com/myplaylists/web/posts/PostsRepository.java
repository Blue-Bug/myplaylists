package com.myplaylists.web.posts;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PostsRepository extends JpaRepository<Posts,Long> {
    Optional<List<Posts>> findByPostsOwner(Member profileMember);

    @Query("SELECT distinct p FROM Posts p join fetch p.postsOwner ORDER BY p.createdAt DESC")
    List<Posts> findAllUsingFetchJoin();


    @Query("SELECT distinct p FROM Posts p join fetch p.postsOwner where p.id = ?1")
    Optional<Posts> findByIdUsingFetchJoin(long postsId);
}
