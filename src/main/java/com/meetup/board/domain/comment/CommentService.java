package com.meetup.board.domain.comment;

import com.meetup.board.common.exception.ForbiddenException;
import com.meetup.board.common.exception.NotFoundException;
import com.meetup.board.domain.comment.dto.CommentCreateRequest;
import com.meetup.board.domain.comment.dto.CommentResponse;
import com.meetup.board.domain.comment.dto.CommentUpdateRequest;
import com.meetup.board.domain.post.StudyPost;
import com.meetup.board.domain.post.StudyPostRepository;
import com.meetup.board.domain.user.User;
import com.meetup.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long postId, Long authorId, CommentCreateRequest request) {
        StudyPost post = studyPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("모임 글을 찾을 수 없습니다. id=" + postId));
        User author = userRepository.getReferenceById(authorId);

        Comment comment = Comment.builder()
                .post(post)
                .author(author)
                .content(request.content())
                .build();

        return commentRepository.save(comment).getId();
    }

    @Transactional(readOnly = true)
    public List<CommentResponse> list(Long postId) {
        return commentRepository.findByPostIdOrderByCreatedAtAsc(postId).stream()
                .map(CommentResponse::from)
                .toList();
    }

    @Transactional
    public void delete(Long commentId, Long requesterId) {
        Comment comment = commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
        if (!comment.isAuthor(requesterId)) {
            throw new ForbiddenException("작성자만 삭제할 수 있습니다.");
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponse update(Long postId, Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new ForbiddenException("본인 댓글만 수정할 수 있습니다.");
        }

        comment.updateContent(request.content());
        return CommentResponse.from(comment);
    }
    }

