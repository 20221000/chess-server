package com.chess.server.repository;

import com.chess.server.entity.Game;
import com.chess.server.entity.GameMove;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameMoveRepository extends JpaRepository<GameMove, Long> {

    // 게임의 전체 착수 기록 조회 (순서대로)
    List<GameMove> findByGameOrderByMoveNumberAsc(Game game);

    // 게임의 총 착수 수 조회
    int countByGame(Game game);

    // 다시하기 시 기존 착수 기록 전체 삭제
    void deleteByGame(Game game);
}