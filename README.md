# 스터디/모임 게시판

정원이 있는 스터디·모임 모집 게시판. 포트폴리오용으로 기능 범위를 의도적으로 좁혔고,
아래 네 가지를 코드로 보여주는 데 초점을 맞췄다.

- JWT 인증 설계
- 쿼리 성능 개선 (인덱스 설계)
- 배치 처리 (마감 스케줄러)
- 부하 테스트 결과 (동시 신청 처리)

## 기능 범위

- 회원가입 / 로그인 / 로그아웃 (JWT)
- 모집글 CRUD + 목록 검색(카테고리·상태·키워드) — 전부 온라인 모집 전제, 지역 구분 없음
- 카테고리는 스터디 / 자격증 / 사이드 프로젝트 3종 고정 (`PostCategory` enum)
- **정원 제한 신청** (동시성 제어 핵심)
- 신청 승인 → **승인자에게만 오픈채팅 링크 노출** (자체 실시간 채팅 대신 채택)
- 댓글 CRUD
- 마감일 경과 모집글 자동 CLOSED 처리 (배치)
- **마이페이지**: 내가 만든 모임 / 내가 신청한 모임 목록

의도적으로 뺀 것: 실시간 채팅, 알림, 좋아요, 태그 추천, 지역 필터. 2주 내 완료 + 4개 핵심
스토리에 집중하기 위한 범위 조정.

## ERD 요약

```
users(id, email, password, nickname, role, created_at)
study_posts(id, author_id, title, content, category, capacity, current_count,
            status, deadline, open_chat_url, created_at, updated_at)
  - category: STUDY / CERTIFICATE / SIDE_PROJECT
applications(id, post_id, applicant_id, status, applied_at)
  - UNIQUE(post_id, applicant_id)
comments(id, post_id, author_id, content, created_at)
```

인덱스:
- `study_posts(category, status)` — 목록 검색용 복합 인덱스
- `study_posts(status, deadline)` — 마감 배치 스캔용
- `comments(post_id, created_at)` — 댓글 목록 조회용
- `applications(post_id, applicant_id)` UNIQUE — 중복 신청 방지 (동시성 최종 방어선)

## 1. JWT 인증 설계

- Access token(30분) / Refresh token(7일) 분리
- Refresh token은 Redis에 `refresh:{userId}` 키로 저장, 재발급마다 로테이션(이전 토큰 폐기)
- 로그아웃 시 access token을 `blacklist:{token}` 키로 Redis에 저장(TTL = 남은 만료시간)하고,
  `JwtAuthenticationFilter`에서 매 요청마다 블랙리스트 체크
- 상세 코드: `auth/JwtTokenProvider`, `auth/TokenRedisService`, `auth/JwtAuthenticationFilter`

## 2. 쿼리 성능 — 정원 신청 동시성 제어

`domain/application/StudyApplicationService.apply()` 참고.

1단계(현재 구현): **비관적 락**
```java
studyPostRepository.findByIdForUpdate(postId) // SELECT ... FOR UPDATE
```
같은 글에 신청이 몰릴 때만 락 대기가 발생하고, 다른 글 신청에는 영향 없음. 구현이 단순해서
1차로 채택.

2단계(비교 대상, TODO): 낙관적 락(`@Version`) + 재시도, Redis(Redisson) 분산락.
부하테스트로 아래를 비교해 README 결과표에 채워 넣을 것:

| 락 전략 | VU | TPS | p95 응답시간 | 실패율(409 제외 순수 에러) | 정합성(정원 초과 발급 여부) |
|---|---|---|---|---|---|
| 비관적 락 | | | | | |
| 낙관적 락 + 재시도 | | | | | |
| Redis 분산락 | | | | | |

## 3. 배치 처리 — 마감 스케줄러

`batch/PostCloseScheduler` — 10분마다 실행.

- 잘못 짜기 쉬운 방식: `findAll()` 후 조건 필터 → 각 엔티티 `save()` → 대상이 늘어나면
  N번의 UPDATE 발생
- 채택한 방식: `@Modifying` JPQL bulk update 1회로 처리 (`StudyPostRepository.closeExpiredPosts`)
- 벌크 업데이트는 영속성 컨텍스트를 우회하므로 `clearAutomatically = true`로 1차 캐시를 비워
  이후 조회의 stale entity 문제를 막음
- `study_posts(status, deadline)` 인덱스로 스캔 범위를 좁힘

## 4. 부하 테스트

`loadtest/apply-test.js` (k6). 정원이 있는 모집글 하나에 정원 수보다 많은 VU로 동시 신청을
쏴서:

- **정합성**: 정원만큼만 200, 나머지는 전부 409(CapacityExceededException)인지
- **성능**: VU를 늘려가며 p95 응답시간과 락 대기시간 변화

```bash
# 1) tokens.example.json을 복사해 tokens.json 생성, 테스트 유저 access token으로 채우기
# 2) 실행
k6 run -e BASE_URL=http://localhost:8080 -e POST_ID=1 loadtest/apply-test.js
```

결과는 위 2번 표에 락 전략별로 기록.

## API 요약

| Method | Path | 설명 | 인증 |
|---|---|---|---|
| POST | /api/auth/signup | 회원가입 | - |
| POST | /api/auth/login | 로그인 | - |
| POST | /api/auth/reissue | 토큰 재발급 | - |
| POST | /api/auth/logout | 로그아웃 | O |
| POST | /api/posts | 모집글 작성 | O |
| GET | /api/posts | 목록 검색 | - |
| GET | /api/posts/{id} | 상세 조회 | - |
| DELETE | /api/posts/{id} | 삭제(작성자) | O |
| POST | /api/posts/{id}/applications | 신청 (정원 제한) | O |
| POST | /api/posts/{id}/applications/{appId}/approve | 신청 승인(작성자) | O |
| DELETE | /api/posts/{id}/applications/{appId} | 신청 취소 | O |
| POST | /api/posts/{id}/comments | 댓글 작성 | O |
| GET | /api/posts/{id}/comments | 댓글 목록 | - |
| DELETE | /api/posts/{id}/comments/{commentId} | 댓글 삭제(작성자) | O |
| GET | /api/users/me/posts | 마이페이지 - 내가 만든 모임 | O |
| GET | /api/users/me/applications | 마이페이지 - 내가 신청한 모임 | O |

## 로컬 실행

DB 비밀번호/JWT secret은 환경변수로 분리돼 있음 (기본값은 `changeme` 등 placeholder라 실제 배포 전 반드시 교체).

```bash
export DB_USERNAME=root
export DB_PASSWORD=your-local-password
export JWT_SECRET=your-long-random-secret

docker compose up -d          # MySQL + Redis
./gradlew bootRun             # Flyway가 기동 시 자동으로 스키마 적용
```

서버가 뜨면 `http://localhost:8080/swagger-ui/index.html` 에서 API를 눈으로 보고 바로
클릭해서 테스트할 수 있음. `/api/auth/signup` → `/api/auth/login`으로 토큰 받고,
우측 상단 Authorize 버튼에 `accessToken` 값만 넣으면 나머지 API도 인증된 채로 호출됨.

> 이 환경에는 Maven Central 접근이 막혀 있어 `./gradlew build`로 직접 컴파일 검증은
> 못 했음. 로컬(또는 CI)에서 빌드해서 컴파일 에러 있으면 잡아줄 것 — 특히 Lombok
> 애노테이션 프로세서 설정, jjwt 0.12.x API 시그니처 정도가 확인 포인트.

## TODO / 다음 개선

- 낙관적 락, Redis 분산락 버전 구현 + 부하테스트 비교표 채우기
- 신청 취소 시 정원(currentCount) 롤백 정책 정하기 (현재는 의도적으로 생략)
- 관리자용 신청자 목록 페이지네이션
