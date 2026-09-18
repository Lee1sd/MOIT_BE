package com.meetup.board.domain.post;

import com.meetup.board.common.exception.ForbiddenException;
import com.meetup.board.common.exception.NotFoundException;
import com.meetup.board.domain.post.dto.PostCreateRequest;
import com.meetup.board.domain.post.dto.PostResponse;
import com.meetup.board.domain.post.dto.PostSummaryResponse;
import com.meetup.board.domain.user.User;
import com.meetup.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyPostService {

    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    @Transactional
    public Long create(Long authorId, PostCreateRequest request) {
        User author = userRepository.getReferenceById(authorId);

        StudyPost post = StudyPost.builder()
                .author(author)
                .title(request.title())
                .content(request.content())
                .category(request.category())
                .capacity(request.capacity())
                .deadline(request.deadline())
                .openChatUrl(request.openChatUrl())
                .build();

        return studyPostRepository.save(post).getId();
    }

    @Transactional(readOnly = true)
    public PostResponse getDetail(Long postId) {
        StudyPost post = findOrThrow(postId);
        return PostResponse.from(post);
    }

    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> search(PostCategory category, PostStatus status, String keyword, Pageable pageable) {
        return studyPostRepository.search(category, status, keyword, pageable)
                .map(PostSummaryResponse::from);
    }

    // 마이페이지 - 내가 만든 모임
    @Transactional(readOnly = true)
    public Page<PostSummaryResponse> getMyPosts(Long authorId, Pageable pageable) {
        return studyPostRepository.findByAuthorId(authorId, pageable)
                .map(PostSummaryResponse::from);
    }

    @Transactional
    public void delete(Long postId, Long requesterId) {
        StudyPost post = findOrThrow(postId);
        if (!post.isAuthor(requesterId)) {
            throw new ForbiddenException("작성자만 삭제할 수 있습니다.");
        }
        studyPostRepository.delete(post);
    }

    private StudyPost findOrThrow(Long postId) {
        return studyPostRepository.findById(postId)
                .orElseThrow(() -> new NotFoundException("모임 글을 찾을 수 없습니다. id=" + postId));
    }
}
