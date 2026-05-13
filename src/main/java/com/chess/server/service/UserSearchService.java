package com.chess.server.service;

import com.chess.server.dto.response.GameRecordResponse;
import com.chess.server.dto.response.UserSearchResponse;
import com.chess.server.entity.GameRecord;
import com.chess.server.entity.User;
import com.chess.server.repository.GameRecordRepository;
import com.chess.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserSearchService {

    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;

    // 닉네임으로 유저 검색
    @Transactional(readOnly = true)
    public UserSearchResponse searchUser(String nickname) {

        User user = userRepository.findByNickname(nickname)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return new UserSearchResponse(user);
    }

    // 특정 유저의 공개 기보 목록 조회
    @Transactional(readOnly = true)
    public List<GameRecordResponse> getUserPublicRecords(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return gameRecordRepository
                .findByOwnerAndVisibilityOrderByCreatedAtDesc(
                        user, GameRecord.RecordVisibility.PUBLIC)
                .stream()
                .map(GameRecordResponse::new)
                .collect(Collectors.toList());
    }
}