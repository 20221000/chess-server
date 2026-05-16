package com.chess.server.repository;

import com.chess.server.entity.Game;
import com.chess.server.entity.GameRoom;
import com.chess.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GameRepository extends JpaRepository<Game, Long> {

    // 방으로 진행 중인 게임 조회
    Optional<Game> findByGameRoomAndStatus(GameRoom gameRoom, Game.GameStatus status);

    // 유저가 참여한 게임 목록 조회 (기보용)
    List<Game> findByWhitePlayerOrBlackPlayerOrderByStartedAtDesc(User whitePlayer, User blackPlayer);

    // 유저가 백으로 참여한 완료된 게임 수
    @Query("SELECT COUNT(g) FROM Game g WHERE g.whitePlayer = :user AND g.status = 'FINISHED' AND g.gameRoom IS NOT NULL")
    int countFinishedAsWhite(@Param("user") User user);

    // 유저가 흑으로 참여한 완료된 게임 수
    @Query("SELECT COUNT(g) FROM Game g WHERE g.blackPlayer = :user AND g.status = 'FINISHED' AND g.gameRoom IS NOT NULL")
    int countFinishedAsBlack(@Param("user") User user);

    // 유저가 백으로 이긴 게임 수
    @Query("SELECT COUNT(g) FROM Game g WHERE g.whitePlayer = :user AND g.result = 'WHITE_WIN' AND g.gameRoom IS NOT NULL")
    int countWinAsWhite(@Param("user") User user);

    // 유저가 흑으로 이긴 게임 수
    @Query("SELECT COUNT(g) FROM Game g WHERE g.blackPlayer = :user AND g.result = 'BLACK_WIN' AND g.gameRoom IS NOT NULL")
    int countWinAsBlack(@Param("user") User user);

    // 유저가 참여한 무승부 게임 수
    @Query("SELECT COUNT(g) FROM Game g WHERE (g.whitePlayer = :user OR g.blackPlayer = :user) AND g.result = 'DRAW' AND g.gameRoom IS NOT NULL")
    int countDraw(@Param("user") User user);
}