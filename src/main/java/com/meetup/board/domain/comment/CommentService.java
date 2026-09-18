package com.meetup.board.domain.comment;

import com.meetup.board.common.exception.BusinessException;
import com.meetup.board.common.exception.ErrorCode;
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
                .orElseThrow(() -> new BusinessException(ErrorCode.POST_NOT_FOUND));
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
                .orElseThrow(() -> new BusinessException(ErrorCode.COMMENT_NOT_FOUND));
        if (!comment.isAuthor(requesterId)) {
            throw new BusinessException(ErrorCode.COMMENT_ACCESS_DENIED);
        }
        commentRepository.delete(comment);
    }

    @Transactional
    public CommentResponse update(Long postId, Long commentId, Long userId, CommentUpdateRequest request) {
        Comment comment = commentRepository.findByIdAndPostId(commentId, postId)
                .orElseThrow(() ->  new BusinessException(ErrorCode.COMMENT_NOT_FOUND));

        if (!comment.getAuthor().getId().equals(userId)) {
            throw new BusinessException(ErrorCode.COMMENT_UPDATE_DENIED);
        }

        comment.updateContent(request.content());
        return CommentResponse.from(comment);
    }
    }

