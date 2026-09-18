package com.meetup.board.auth;

import com.meetup.board.auth.dto.*;
import com.meetup.board.common.exception.BusinessException;
import com.meetup.board.domain.user.User;
import com.meetup.board.domain.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final TokenRedisService tokenRedisService;

    @Transactional
    public void signup(SignupRequest request) {
        if (userRepository.existsByEmail(request.email())) {
            throw new BusinessException("이미 가입된 이메일입니다.", HttpStatus.CONFLICT);
        }
        User user = User.builder()
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .nickname(request.nickname())
                .build();
        userRepository.save(user);
    }

    @Transactional(readOnly = true)
    public TokenResponse login(LoginRequest request) {
        User user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new BusinessException("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED));

        if (!passwordEncoder.matches(request.password(), user.getPassword())) {
            throw new BusinessException("이메일 또는 비밀번호가 올바르지 않습니다.", HttpStatus.UNAUTHORIZED);
        }

        return issueTokens(user.getId(), user.getRole().name());
    }

    // refresh token 로테이션: 재발급 시마다 새 refresh를 발급하고 이전 것은 폐기
    @Transactional(readOnly = true)
    public TokenResponse reissue(ReissueRequest request) {
        String refreshToken = request.refreshToken();

        if (!jwtTokenProvider.isValid(refreshToken)) {
            throw new BusinessException("유효하지 않은 refresh token 입니다.", HttpStatus.UNAUTHORIZED);
        }

        Long userId = jwtTokenProvider.getUserId(refreshToken);

        if (!tokenRedisService.isRefreshTokenValid(userId, refreshToken)) {
            throw new BusinessException("만료되었거나 이미 사용된 refresh token 입니다.", HttpStatus.UNAUTHORIZED);
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException("사용자를 찾을 수 없습니다.", HttpStatus.UNAUTHORIZED));

        return issueTokens(user.getId(), user.getRole().name());
    }

    public void logout(Long userId, String accessToken) {
        tokenRedisService.deleteRefreshToken(userId);
        long remaining = jwtTokenProvider.getRemainingMillis(accessToken);
        tokenRedisService.blacklistAccessToken(accessToken, remaining);
    }

    private TokenResponse issueTokens(Long userId, String role) {
        String accessToken = jwtTokenProvider.createAccessToken(userId, role);
        String refreshToken = jwtTokenProvider.createRefreshToken(userId);
        tokenRedisService.saveRefreshToken(userId, refreshToken, jwtTokenProvider.getRefreshTokenExpirationMs());
        return new TokenResponse(accessToken, refreshToken);
    }
}
