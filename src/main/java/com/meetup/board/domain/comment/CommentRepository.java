package com.meetup.board.domain.comment;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    // idx_comments_post(post_id, created_at) 인덱스 사용
    List<Comment> findByPostIdOrderByCreatedAtAsc(Long postId);
    Optional<Comment> findByIdAndPostId(Long id, Long postId);
}
