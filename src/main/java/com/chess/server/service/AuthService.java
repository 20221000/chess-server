package com.chess.server.service;

import com.chess.server.dto.request.LoginRequest;
import com.chess.server.dto.request.SignupRequest;
import com.chess.server.dto.response.AuthResponse;
import com.chess.server.entity.User;
import com.chess.server.exception.DuplicateException;
import com.chess.server.exception.NotFoundException;
import com.chess.server.exception.UnauthorizedException;
import com.chess.server.repository.UserRepository;
import com.chess.server.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

// @RequiredArgsConstructor : final 필드를 파라미터로 받는 생성자 자동 생성
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    // 회원가입
    @Transactional
    public void signup(SignupRequest request) {

        // 중복 확인
        if (userRepository.existsByUsername(request.getUsername())) {
            throw new DuplicateException("이미 사용 중인 아이디입니다.");
        }
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateException("이미 사용 중인 닉네임입니다.");
        }
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("이미 사용 중인 이메일입니다.");
        }

        // 비밀번호 암호화 후 저장
        User user = User.builder()
                .username(request.getUsername())
                .password(passwordEncoder.encode(request.getPassword()))
                .nickname(request.getNickname())
                .email(request.getEmail())
                .build();

        userRepository.save(user);
    }

    // 로그인
    @Transactional(readOnly = true)
    public AuthResponse login(LoginRequest request) {

        // 유저 존재 확인
        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new NotFoundException("존재하지 않는 아이디입니다."));

        // 비밀번호 확인
        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new UnauthorizedException("아이디 또는 비밀번호가 틀렸습니다.");
        }

        // JWT 토큰 발급 후 반환
        String token = jwtUtil.generateToken(user.getUsername());
        return new AuthResponse(token, user.getNickname(), user.getUsername());
    }
}