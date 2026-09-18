package com.meetup.board.domain.post;

import com.meetup.board.domain.post.dto.PostCreateRequest;
import com.meetup.board.domain.post.dto.PostResponse;
import com.meetup.board.domain.post.dto.PostSummaryResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
public class StudyPostController {

    private final StudyPostService studyPostService;

    @PostMapping("/api/posts")
    public ResponseEntity<Long> create(@AuthenticationPrincipal Long userId, @Valid @RequestBody PostCreateRequest request) {
        return ResponseEntity.ok(studyPostService.create(userId, request));
    }

    @GetMapping("/api/posts/{postId}")
    public ResponseEntity<PostResponse> getDetail(@PathVariable Long postId) {
        return ResponseEntity.ok(studyPostService.getDetail(postId));
    }

    @GetMapping("/api/posts")
    public ResponseEntity<Page<PostSummaryResponse>> search(
            @RequestParam(required = false) PostCategory category,
            @RequestParam(required = false) PostStatus status,
            @RequestParam(required = false) String keyword,
            Pageable pageable
    ) {
        return ResponseEntity.ok(studyPostService.search(category, status, keyword, pageable));
    }

    @DeleteMapping("/api/posts/{postId}")
    public ResponseEntity<Void> delete(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        studyPostService.delete(postId, userId);
        return ResponseEntity.noContent().build();
    }
}
