CREATE TABLE users (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    email       VARCHAR(100)  NOT NULL,
    password    VARCHAR(255)  NOT NULL,
    nickname    VARCHAR(50)   NOT NULL,
    role        VARCHAR(20)   NOT NULL DEFAULT 'USER',
    created_at  DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE KEY uk_users_email (email)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE study_posts (
    id             BIGINT AUTO_INCREMENT PRIMARY KEY,
    author_id      BIGINT        NOT NULL,
    title          VARCHAR(200)  NOT NULL,
    content        TEXT          NOT NULL,
    category       VARCHAR(30)   NOT NULL,
    capacity       INT           NOT NULL,
    current_count  INT           NOT NULL DEFAULT 0,
    status         VARCHAR(20)   NOT NULL DEFAULT 'OPEN',
    deadline       DATETIME      NOT NULL,
    open_chat_url  VARCHAR(255),
    created_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
    CONSTRAINT fk_posts_author FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

-- 목록 검색(카테고리+상태) 커버 인덱스
CREATE INDEX idx_posts_category_status ON study_posts (category, status);
-- 마감 배치 스캔용 인덱스 (status='OPEN' AND deadline < now)
CREATE INDEX idx_posts_status_deadline ON study_posts (status, deadline);

CREATE TABLE applications (
    id            BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id       BIGINT       NOT NULL,
    applicant_id  BIGINT       NOT NULL,
    status        VARCHAR(20)  NOT NULL DEFAULT 'APPLIED',
    applied_at    DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_app_post FOREIGN KEY (post_id) REFERENCES study_posts(id),
    CONSTRAINT fk_app_user FOREIGN KEY (applicant_id) REFERENCES users(id),
    UNIQUE KEY uk_app_post_user (post_id, applicant_id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE TABLE comments (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    post_id     BIGINT       NOT NULL,
    author_id   BIGINT       NOT NULL,
    content     VARCHAR(1000) NOT NULL,
    created_at  DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at   DATETIME      NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_comment_post FOREIGN KEY (post_id) REFERENCES study_posts(id),
    CONSTRAINT fk_comment_user FOREIGN KEY (author_id) REFERENCES users(id)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4;

CREATE INDEX idx_comments_post ON comments (post_id, created_at);
