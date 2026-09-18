package com.meetup.board.domain.application;

import com.meetup.board.domain.post.StudyPost;
import com.meetup.board.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "applications", uniqueConstraints = {
        @UniqueConstraint(name = "uk_app_post_user", columnNames = {"post_id", "applicant_id"})
})
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyApplication {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private StudyPost post;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "applicant_id", nullable = false)
    private User applicant;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private ApplicationStatus status;

    @Column(name = "applied_at", nullable = false, updatable = false)
    private LocalDateTime appliedAt;

    @Builder
    public StudyApplication(StudyPost post, User applicant) {
        this.post = post;
        this.applicant = applicant;
        this.status = ApplicationStatus.APPLIED;
    }

    @PrePersist
    void prePersist() {
        this.appliedAt = LocalDateTime.now();
    }

}
