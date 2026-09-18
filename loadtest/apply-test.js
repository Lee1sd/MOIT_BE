// k6 부하 테스트 - 정원 마감이 있는 신청(apply) API에 동시 요청을 몰아넣는 시나리오.
// 실행 전: 테스트용 유저 여러 명을 만들어 토큰을 발급받고 tokens.json 같은 파일로 준비해두거나,
// 아래 setup()에서 로그인까지 직접 수행하도록 채워 넣을 것.
//
// 실행: k6 run loadtest/apply-test.js
//
// 확인할 것:
//  - 정원(capacity)만큼만 200이 나오고 나머지는 전부 409(CapacityExceededException)인지 (정합성)
//  - VU 수를 늘려가며 p95 응답시간 / 락 대기시간 변화 (성능)
//  - 락 전략(비관적 락 / 낙관적 락+재시도 / Redis 분산락)을 바꿔가며 같은 시나리오로 재측정 → README 결과표에 기록

import http from 'k6/http';
import { check } from 'k6';

const BASE_URL = __ENV.BASE_URL || 'http://localhost:8080';
const POST_ID = __ENV.POST_ID || '1';

// 사전에 회원가입/로그인해서 뽑아둔 access token 목록.
// VU 수만큼 채워야 각 가상유저가 서로 다른 사용자로 신청함 (동일 유저 중복신청은 409로 걸러짐).
const TOKENS = JSON.parse(open('./tokens.json'));

export const options = {
    scenarios: {
        concurrent_apply: {
            executor: 'per-vu-iterations',
            vus: TOKENS.length,
            iterations: 1,
            maxDuration: '30s',
        },
    },
};

export default function () {
    const token = TOKENS[__VU - 1];

    const res = http.post(
        `${BASE_URL}/api/posts/${POST_ID}/applications`,
        null,
        { headers: { Authorization: `Bearer ${token}` } }
    );

    check(res, {
        'status is 200 or 409': (r) => r.status === 200 || r.status === 409,
    });
}
