package com.chess.server.dto.response;

import com.chess.server.entity.GamePlayer;
import com.chess.server.entity.GameRoom;
import lombok.Getter;

import java.util.List;
import java.util.stream.Collectors;

@Getter
public class RoomStateResponse {

    private Long roomId;
    private String status;
    private String hostNickname;
    // 방장이 선택한 색상 → 프론트에서 UI 표시용
    private String hostColor;
    private List<PlayerInfo> players;
    private List<PlayerInfo> spectators;

    public RoomStateResponse(GameRoom gameRoom, List<GamePlayer> players, List<GamePlayer> spectators) {
        this.roomId = gameRoom.getId();
        this.status = gameRoom.getStatus().name();
        this.hostNickname = gameRoom.getHost().getNickname();
        this.hostColor = gameRoom.getHostColor().name();
        this.players = players.stream()
                .map(PlayerInfo::new)
                .collect(Collectors.toList());
        this.spectators = spectators.stream()
                .map(PlayerInfo::new)
                .collect(Collectors.toList());
    }

    @Getter
    public static class PlayerInfo {
        private Long userId;
        private String nickname;
        private String status;
        private String pieceColor;

        public PlayerInfo(GamePlayer gamePlayer) {
            this.userId = gamePlayer.getUser().getId();
            this.nickname = gamePlayer.getUser().getNickname();
            this.status = gamePlayer.getStatus().name();
            this.pieceColor = gamePlayer.getPieceColor() != null
                    ? gamePlayer.getPieceColor().name()
                    : null;
        }
    }
}