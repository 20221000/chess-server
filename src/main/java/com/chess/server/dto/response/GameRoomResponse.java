package com.chess.server.dto.response;

import com.chess.server.entity.GameRoom;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameRoomResponse {

    private Long id;
    private String title;           // 방 제목
    private int maxPlayers;         // 최대 인원
    private int currentPlayers;     // 현재 인원
    private String status;          // 방 상태
    private String hostNickname;    // 방장 닉네임
    private LocalDateTime createdAt;

    // GameRoom 엔티티 → DTO 변환
    public GameRoomResponse(GameRoom gameRoom, int currentPlayers) {
        this.id = gameRoom.getId();
        this.title = gameRoom.getTitle();
        this.maxPlayers = gameRoom.getMaxPlayers();
        this.currentPlayers = currentPlayers;
        this.status = gameRoom.getStatus().name();
        this.hostNickname = gameRoom.getHost().getNickname();
        this.createdAt = gameRoom.getCreatedAt();
    }
}