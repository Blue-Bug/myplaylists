package com.myplaylists.web.posts;

import com.myplaylists.domain.Member;
import com.myplaylists.domain.Posts;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;

import javax.persistence.FetchType;
import javax.persistence.ManyToOne;
import java.util.List;
import java.util.Optional;

@Transactional(readOnly = true)
public interface PostsRepository extends JpaRepository<Posts,Long> {

    Optional<List<Posts>> findByPostsOwner(Member profileMember);

    //페이징이랑 페치 조인 사용불가 (이경우는 다대일을 페치 조인했기때문에 가능)
    @Query(value = "SELECT distinct p FROM Posts p " +
                   "join fetch p.postsOwner " +
                   "ORDER BY p.createdAt DESC"
     ,countQuery = "SELECT count(p.title) from Posts p")
    Page<Posts> findAllUsingFetchJoin(Pageable pageable);


    //둘 이상의 컬렉션은 페치 조인 불가능(Link,Comment 는 따로 조회해야함)
    @Query("SELECT distinct p FROM Posts p " +
            "join fetch p.postsOwner " +
            "join fetch p.playlists " +
            "where p.id = ?1")
    Optional<Posts> findByIdUsingFetchJoin(long postsId);


    //페이징이랑 페치 조인 사용불가
    //해결 방법 -> To One 은 일단 다 fetch 나머지는 BatchSize 설정 사용
    @Query(value = "SELECT p FROM Posts p " +
            "join fetch p.postsOwner " +
            "where p.title like %:keyword% ORDER BY p.createdAt DESC"
     ,countQuery = "SELECT count(p) FROM Posts p where p.title like %:keyword%")
    Page<Posts> findByTitleContaining(@Param("keyword") String keyword, Pageable pageable);
}
