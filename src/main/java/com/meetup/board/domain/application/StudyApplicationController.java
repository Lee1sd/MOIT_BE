package com.meetup.board.domain.application;

import com.meetup.board.domain.application.dto.ApplicationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/posts/{postId}/applications")
@RequiredArgsConstructor
public class StudyApplicationController {

    private final StudyApplicationService studyApplicationService;

    @PostMapping
    public ResponseEntity<ApplicationResponse> apply(@AuthenticationPrincipal Long userId, @PathVariable Long postId) {
        return ResponseEntity.ok(studyApplicationService.apply(postId, userId));
    }


    @DeleteMapping("/{applicationId}")
    public ResponseEntity<Void> cancel(
            @AuthenticationPrincipal Long userId,
            @PathVariable Long postId,
            @PathVariable Long applicationId
    ) {
        studyApplicationService.cancel(postId, applicationId, userId);
        return ResponseEntity.noContent().build();
    }
}
