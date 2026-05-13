package com.chess.server.repository;

import com.chess.server.entity.GamePlayer;
import com.chess.server.entity.GameRoom;
import com.chess.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface GamePlayerRepository extends JpaRepository<GamePlayer, Long> {

    // 방의 전체 플레이어 목록 조회
    List<GamePlayer> findByGameRoom(GameRoom gameRoom);

    // 특정 유저가 특정 방에 있는지 확인
    Optional<GamePlayer> findByGameRoomAndUser(GameRoom gameRoom, User user);

    // 방의 플레이어 수 조회
    int countByGameRoom(GameRoom gameRoom);

    // 방의 준비 완료 플레이어 수 조회
    int countByGameRoomAndStatus(GameRoom gameRoom, GamePlayer.PlayerStatus status);
}