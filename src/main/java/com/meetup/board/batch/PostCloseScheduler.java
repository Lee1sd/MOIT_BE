package com.meetup.board.batch;

import com.meetup.board.domain.post.StudyPostRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * 마감일이 지난 OPEN 상태 글을 CLOSED로 전환하는 배치.
 *
 * 처음엔 findAll -> 조건 필터 -> 1건씩 save 로 짜기 쉬운데,
 * 대상 건수가 늘어나면 N번의 UPDATE 쿼리가 나가서 느려짐.
 * 여기서는 JPQL bulk update(StudyPostRepository#closeExpiredPosts) 한 번으로 처리.
 * (단, 벌크 업데이트는 영속성 컨텍스트를 거치지 않으므로 @Modifying(clearAutomatically = true)로
 *  1차 캐시를 비워 이후 조회에서 stale entity를 읽지 않도록 함)
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class PostCloseScheduler {

    private final StudyPostRepository studyPostRepository;

    @Transactional
    @Scheduled(cron = "0 */10 * * * *") // 10분마다
    public void closeExpiredPosts() {
        int updated = studyPostRepository.closeExpiredPosts(LocalDateTime.now());
        if (updated > 0) {
            log.info("마감일 경과로 CLOSED 처리된 모집글 수: {}", updated);
        }
    }
}
