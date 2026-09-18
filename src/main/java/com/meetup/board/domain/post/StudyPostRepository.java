package com.meetup.board.domain.post;

import jakarta.persistence.LockModeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.Optional;

public interface StudyPostRepository extends JpaRepository<StudyPost, Long> {

    // 정원 마감 동시성 제어 1단계: 비관적 락(SELECT ... FOR UPDATE)
    // 신청이 몰리는 소수의 인기 글에 대해서만 락 대기가 발생하므로 우선 이걸로 시작하고,
    // 부하테스트로 대기시간이 문제가 되면 Redis 분산락 / 낙관적 락 재시도로 교체 검토.
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select p from StudyPost p where p.id = :id")
    Optional<StudyPost> findByIdForUpdate(@Param("id") Long id);

    // 목록 검색 - category/status 복합 인덱스(idx_posts_category_status)를 타도록 조건 순서를 인덱스와 맞춤
    @Query("""
            select p from StudyPost p
            where (:category is null or p.category = :category)
              and (:status is null or p.status = :status)
              and (:keyword is null or p.title like concat('%', :keyword, '%'))
            order by p.createdAt desc
            """)
    Page<StudyPost> search(
            @Param("category") PostCategory category,
            @Param("status") PostStatus status,
            @Param("keyword") String keyword,
            Pageable pageable
    );

    // 마이페이지 - 내가 만든 모임
    Page<StudyPost> findByAuthorId(Long authorId, Pageable pageable);

    // 마감 배치용 벌크 업데이트 - 대상이 N건이어도 UPDATE 1회로 처리 (idx_posts_status_deadline 사용)
    @Modifying(clearAutomatically = true)
    @Query("update StudyPost p set p.status = 'CLOSED' where p.status = 'OPEN' and p.deadline < :now")
    int closeExpiredPosts(@Param("now") LocalDateTime now);
}
