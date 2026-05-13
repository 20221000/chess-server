package com.chess.server.repository;

import com.chess.server.entity.GameRoom;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface GameRoomRepository extends JpaRepository<GameRoom, Long> {

    // 상태별 방 목록 조회 (로비에서 대기중인 방 목록)
    List<GameRoom> findByStatusOrderByCreatedAtDesc(GameRoom.RoomStatus status);
}