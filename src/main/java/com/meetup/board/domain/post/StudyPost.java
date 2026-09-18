package com.meetup.board.domain.post;

import com.meetup.board.common.exception.BusinessException;
import com.meetup.board.common.exception.ErrorCode;
import com.meetup.board.domain.user.User;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "study_posts")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class StudyPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    @Column(nullable = false, length = 200)
    private String title;

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private PostCategory category;

    @Column(nullable = false)
    private int capacity;

    @Column(name = "current_count", nullable = false)
    private int currentCount;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private PostStatus status;

    @Column(nullable = false)
    private LocalDateTime deadline;

    @Column(name = "open_chat_url")
    private String openChatUrl;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Builder
    public StudyPost(User author, String title, String content, PostCategory category,
                      int capacity, LocalDateTime deadline, String openChatUrl) {
        this.author = author;
        this.title = title;
        this.content = content;
        this.category = category;
        this.capacity = capacity;
        this.currentCount = 0;
        this.status = PostStatus.OPEN;
        this.deadline = deadline;
        this.openChatUrl = openChatUrl;
    }

    @PrePersist
    void prePersist() {
        this.createdAt = LocalDateTime.now();
        this.updatedAt = LocalDateTime.now();
    }

    @PreUpdate
    void preUpdate() {
        this.updatedAt = LocalDateTime.now();
    }

    // 비관적 락으로 조회한 상태에서만 호출되어야 안전함 (StudyApplicationService 참고)
    public void applyOneSeat() {
        if (this.status != PostStatus.OPEN) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }
        if (this.currentCount >= this.capacity) {
            throw new BusinessException(ErrorCode.CAPACITY_EXCEEDED);
        }
        this.currentCount += 1;
        if (this.currentCount >= this.capacity) {
            this.status = PostStatus.CLOSED;
        }
    }

    public boolean isAuthor(Long userId) {
        return this.author.getId().equals(userId);
    }
}
