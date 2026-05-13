package com.chess.server.service;

import com.chess.server.dto.request.UpdateEmailRequest;
import com.chess.server.dto.request.UpdateNicknameRequest;
import com.chess.server.dto.request.UpdatePasswordRequest;
import com.chess.server.dto.response.UserProfileResponse;
import com.chess.server.dto.response.UserStatsResponse;
import com.chess.server.entity.User;
import com.chess.server.exception.DuplicateException;
import com.chess.server.exception.NotFoundException;
import com.chess.server.exception.UnauthorizedException;
import com.chess.server.repository.GameRepository;
import com.chess.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final GameRepository gameRepository;
    private final PasswordEncoder passwordEncoder;

    // 내 정보 조회
    @Transactional(readOnly = true)
    public UserProfileResponse getMyProfile(String username) {
        User user = findUserByUsername(username);
        return new UserProfileResponse(user);
    }

    // 닉네임 수정
    @Transactional
    public void updateNickname(String username, UpdateNicknameRequest request) {
        User user = findUserByUsername(username);

        // 현재 닉네임과 동일한지 확인
        if (user.getNickname().equals(request.getNickname())) {
            throw new DuplicateException("현재 사용 중인 닉네임입니다.");
        }

        // 다른 유저가 사용 중인지 확인
        if (userRepository.existsByNickname(request.getNickname())) {
            throw new DuplicateException("이미 사용 중인 닉네임입니다.");
        }

        user.updateNickname(request.getNickname());
    }

    // 비밀번호 수정
    @Transactional
    public void updatePassword(String username, UpdatePasswordRequest request) {
        User user = findUserByUsername(username);

        // 현재 비밀번호 확인
        if (!passwordEncoder.matches(request.getCurrentPassword(), user.getPassword())) {
            throw new UnauthorizedException("현재 비밀번호가 틀렸습니다.");
        }

        // 현재 비밀번호와 새 비밀번호 동일 여부 확인
        if (passwordEncoder.matches(request.getNewPassword(), user.getPassword())) {
            throw new DuplicateException("현재 비밀번호와 동일한 비밀번호로 변경할 수 없습니다.");
        }

        user.updatePassword(passwordEncoder.encode(request.getNewPassword()));
    }

    // 이메일 수정
    @Transactional
    public void updateEmail(String username, UpdateEmailRequest request) {
        User user = findUserByUsername(username);

        // 다른 유저가 사용 중인지 확인 (null이면 삭제 처리)
        if (request.getEmail() != null && userRepository.existsByEmail(request.getEmail())) {
            throw new DuplicateException("이미 사용 중인 이메일입니다.");
        }

        user.updateEmail(request.getEmail());
    }

    // 전적 조회
    @Transactional(readOnly = true)
    public UserStatsResponse getMyStats(String username) {
        User user = findUserByUsername(username);

        // 백/흑으로 참여한 승리 합산
        int wins = gameRepository.countWinAsWhite(user) + gameRepository.countWinAsBlack(user);

        // 총 완료 게임 - 승리 - 무승부 = 패배
        int totalFinished = gameRepository.countFinishedAsWhite(user) + gameRepository.countFinishedAsBlack(user);
        int draws = gameRepository.countDraw(user);
        int losses = totalFinished - wins - draws;

        return new UserStatsResponse(wins, losses, draws);
    }

    // username으로 유저 조회 (내부 공통 메서드)
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
    }

    // 회원 탈퇴
    @Transactional
    public void withdraw(String username) {
        User user = findUserByUsername(username);
        user.withdraw();
    }
}