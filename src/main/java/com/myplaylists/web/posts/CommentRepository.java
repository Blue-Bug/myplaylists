package com.myplaylists.web.posts;

import com.myplaylists.domain.Comment;
import com.myplaylists.domain.Posts;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment,Long> {
    @Query(value = "SELECT c FROM Comment c " +
            "WHERE c.commentedPosts = ?1 " +
            "ORDER BY c.createdAt ASC")
    Optional<List<Comment>> findByCommentedPosts(Posts commentedPosts);
}
