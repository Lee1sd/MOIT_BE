package com.meetup.board.mypage;

import com.meetup.board.domain.application.StudyApplicationService;
import com.meetup.board.domain.application.dto.MyApplicationResponse;
import com.meetup.board.domain.post.StudyPostService;
import com.meetup.board.domain.post.dto.PostSummaryResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

// 마이페이지: "내가 만든 모임"은 StudyPostService, "내가 신청한 모임"은 StudyApplicationService에
// 이미 있는 조회 로직을 그대로 재사용 - 여기서는 두 도메인을 엮어 보여주는 진입점 역할만 함.
@RestController
@RequestMapping("/api/users/me")
@RequiredArgsConstructor
public class MyPageController {

    private final StudyPostService studyPostService;
    private final StudyApplicationService studyApplicationService;

    @GetMapping("/posts")
    public ResponseEntity<Page<PostSummaryResponse>> getMyPosts(
            @AuthenticationPrincipal Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(studyPostService.getMyPosts(userId, pageable));
    }

    @GetMapping("/applications")
    public ResponseEntity<Page<MyApplicationResponse>> getMyApplications(
            @AuthenticationPrincipal Long userId,
            Pageable pageable
    ) {
        return ResponseEntity.ok(studyApplicationService.getMyApplications(userId, pageable));
    }
}
