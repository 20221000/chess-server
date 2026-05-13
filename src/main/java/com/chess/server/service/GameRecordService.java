package com.chess.server.service;

import com.chess.server.dto.request.UpdateRecordRequest;
import com.chess.server.dto.response.GameMoveResponse;
import com.chess.server.dto.response.GameRecordResponse;
import com.chess.server.entity.*;
import com.chess.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRecordService {

    private final GameRecordRepository gameRecordRepository;
    private final SavedRecordRepository savedRecordRepository;
    private final GameMoveRepository gameMoveRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    // 게임 종료 시 기보 자동 저장 (참여자용)
    @Transactional
    public void saveRecord(Long gameId, String username) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("게임을 찾을 수 없습니다."));

        User owner = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 이미 저장된 기보인지 확인
        if (gameRecordRepository.findByGame(game).isPresent()) {
            throw new IllegalArgumentException("이미 저장된 기보입니다.");
        }

        GameRecord gameRecord = GameRecord.builder()
                .game(game)
                .owner(owner)
                .title(game.getWhitePlayer().getNickname() + " vs " +
                        game.getBlackPlayer().getNickname())
                .build();

        gameRecordRepository.save(gameRecord);
    }

    // 남의 기보 저장
    @Transactional
    public void saveOthersRecord(Long recordId, String username) {

        GameRecord gameRecord = gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기보를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 비공개 기보 저장 불가
        if (gameRecord.getVisibility() == GameRecord.RecordVisibility.PRIVATE) {
            throw new IllegalArgumentException("비공개 기보입니다.");
        }

        // 이미 저장한 기보인지 확인
        if (savedRecordRepository.existsByUserAndGameRecord(user, gameRecord)) {
            throw new IllegalArgumentException("이미 저장한 기보입니다.");
        }

        SavedRecord savedRecord = SavedRecord.builder()
                .user(user)
                .gameRecord(gameRecord)
                .build();

        savedRecordRepository.save(savedRecord);
    }

    // 내 기보 목록 조회
    @Transactional(readOnly = true)
    public List<GameRecordResponse> getMyRecords(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return gameRecordRepository.findByOwnerOrderByCreatedAtDesc(user)
                .stream()
                .map(GameRecordResponse::new)
                .collect(Collectors.toList());
    }

    // 저장한 기보 목록 조회
    @Transactional(readOnly = true)
    public List<GameRecordResponse> getSavedRecords(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        return savedRecordRepository.findByUserOrderBySavedAtDesc(user)
                .stream()
                .map(saved -> new GameRecordResponse(saved.getGameRecord()))
                .collect(Collectors.toList());
    }

    // 기보 재생 (착수 목록 조회)
    @Transactional(readOnly = true)
    public List<GameMoveResponse> getGameMoves(Long recordId, String username) {

        GameRecord gameRecord = gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기보를 찾을 수 없습니다."));

        // 비공개 기보는 소유자만 조회 가능
        if (gameRecord.getVisibility() == GameRecord.RecordVisibility.PRIVATE) {
            User user = userRepository.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));
            if (!gameRecord.getOwner().getId().equals(user.getId())) {
                throw new IllegalArgumentException("비공개 기보입니다.");
            }
        }

        return gameMoveRepository.findByGameOrderByMoveNumberAsc(gameRecord.getGame())
                .stream()
                .map(GameMoveResponse::new)
                .collect(Collectors.toList());
    }

    // 기보 제목 / 공개 여부 수정
    @Transactional
    public GameRecordResponse updateRecord(Long recordId, UpdateRecordRequest request, String username) {

        GameRecord gameRecord = gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기보를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 소유자만 수정 가능
        if (!gameRecord.getOwner().getId().equals(user.getId())) {
            throw new IllegalArgumentException("본인의 기보만 수정할 수 있습니다.");
        }

        if (request.getTitle() != null) {
            gameRecord.updateTitle(request.getTitle());
        }
        if (request.getVisibility() != null) {
            gameRecord.updateVisibility(
                    GameRecord.RecordVisibility.valueOf(request.getVisibility()));
        }

        return new GameRecordResponse(gameRecordRepository.save(gameRecord));
    }

    // 저장한 남의 기보 삭제
    @Transactional
    public void deleteSavedRecord(Long recordId, String username) {

        GameRecord gameRecord = gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new IllegalArgumentException("기보를 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        SavedRecord savedRecord = savedRecordRepository.findByUserAndGameRecord(user, gameRecord)
                .orElseThrow(() -> new IllegalArgumentException("저장한 기보가 아닙니다."));

        savedRecordRepository.delete(savedRecord);
    }
}