package com.chess.server.repository;

import com.chess.server.entity.Game;
import com.chess.server.entity.GameRoom;
import com.chess.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GameRepository extends JpaRepository<Game, Long> {

    // 방으로 게임 조회
    Optional<Game> findByGameRoom(GameRoom gameRoom);

    // 유저가 참여한 게임 목록 조회 (기보용)
    List<Game> findByWhitePlayerOrBlackPlayerOrderByStartedAtDesc(User whitePlayer, User blackPlayer);
}