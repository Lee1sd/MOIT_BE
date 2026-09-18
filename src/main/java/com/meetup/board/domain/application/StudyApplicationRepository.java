package com.meetup.board.domain.application;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface StudyApplicationRepository extends JpaRepository<StudyApplication, Long> {
    boolean existsByPostIdAndApplicantId(Long postId, Long applicantId);
    Optional<StudyApplication> findByIdAndPostId(Long id, Long postId);
    List<StudyApplication> findByPostId(Long postId);

    // 마이페이지 - 내가 신청한 모임. post/author를 함께 fetch해서 화면에 필요한 정보를 한 번에 (N+1 방지)
    @Query("""
            select a from StudyApplication a
            join fetch a.post p
            join fetch p.author
            where a.applicant.id = :applicantId
            order by a.appliedAt desc
            """)
    Page<StudyApplication> findByApplicantIdWithPost(Long applicantId, Pageable pageable);
}
