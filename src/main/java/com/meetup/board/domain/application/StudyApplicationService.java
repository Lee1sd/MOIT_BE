package com.meetup.board.domain.application;

import com.meetup.board.common.exception.DuplicateApplicationException;
import com.meetup.board.common.exception.ForbiddenException;
import com.meetup.board.common.exception.NotFoundException;
import com.meetup.board.domain.application.dto.ApplicationResponse;
import com.meetup.board.domain.application.dto.MyApplicationResponse;
import com.meetup.board.domain.post.StudyPost;
import com.meetup.board.domain.post.StudyPostRepository;
import com.meetup.board.domain.user.User;
import com.meetup.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StudyApplicationService {

    private final StudyApplicationRepository studyApplicationRepository;
    private final StudyPostRepository studyPostRepository;
    private final UserRepository userRepository;

    /**
     * 정원 제한 신청 - 동시성 제어의 핵심.
     *
     * 1) findByIdForUpdate 로 study_posts row에 PESSIMISTIC_WRITE 락을 건다.
     *    -> 같은 글에 대한 동시 요청은 이 트랜잭션이 끝날 때까지 대기한다.
     * 2) 락을 잡은 상태에서 정원/상태를 확인하고 currentCount를 1 증가시킨다.
     * 3) (post_id, applicant_id) unique 제약이 중복 신청의 최종 방어선 (동시에 두 번 눌러도 하나는 DB에서 튕김).
     *
     * 트래픽이 몰리는 인기 글에서 락 대기시간이 문제가 되면:
     *  - 낙관적 락(@Version) + 재시도 로 전환하거나
     *  - Redis(Redisson) 분산락으로 애플리케이션 레벨에서 짧게 잠그는 방식으로 교체 검토.
     * 이 프로젝트에서는 세 방식을 모두 구현해 부하테스트로 TPS/실패율을 비교한다. (README 참고)
     */
    @Transactional
    public ApplicationResponse apply(Long postId, Long applicantId) {
        if (studyApplicationRepository.existsByPostIdAndApplicantId(postId, applicantId)) {
            throw new DuplicateApplicationException();
        }

        StudyPost post = studyPostRepository.findByIdForUpdate(postId)
                .orElseThrow(() -> new NotFoundException("모임 글을 찾을 수 없습니다. id=" + postId));

        post.applyOneSeat(); // 정원 초과 시 CapacityExceededException

        User applicant = userRepository.getReferenceById(applicantId);

        StudyApplication application = StudyApplication.builder()
                .post(post)
                .applicant(applicant)
                .build();

        studyApplicationRepository.save(application);

        return ApplicationResponse.from(application);
    }


    @Transactional
    public void cancel(Long postId, Long applicationId, Long requesterId) {
        StudyApplication application = studyApplicationRepository.findByIdAndPostId(applicationId, postId)
                .orElseThrow(() -> new NotFoundException("신청 내역을 찾을 수 없습니다."));

        if (!application.getApplicant().getId().equals(requesterId)) {
            throw new ForbiddenException("본인 신청만 취소할 수 있습니다.");
        }

        // 정원 롤백은 의도적으로 생략: 자리 반환은 관리자 승인 취소 정책과 함께 별도 이슈로 다룸 (README TODO 참고)
        studyApplicationRepository.delete(application);
    }

    // 마이페이지 - 내가 신청한 모임
    @Transactional(readOnly = true)
    public Page<MyApplicationResponse> getMyApplications(Long applicantId, Pageable pageable) {
        return studyApplicationRepository.findByApplicantIdWithPost(applicantId, pageable)
                .map(MyApplicationResponse::from);
    }
}
