package com.chess.server.repository;

import com.chess.server.entity.Game;
import com.chess.server.entity.GameRecord;
import com.chess.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRecordRepository extends JpaRepository<GameRecord, Long> {

    // 내 기보 목록 조회
    List<GameRecord> findByOwnerOrderByCreatedAtDesc(User owner);

    // 공개된 특정 유저 기보 목록 조회 (유저 검색 시)
    List<GameRecord> findByOwnerAndVisibilityOrderByCreatedAtDesc(
            User owner, GameRecord.RecordVisibility visibility);

    // 게임으로 기보 조회
    Optional<GameRecord> findByGame(Game game);
}