package com.chess.server.dto.response;

import com.chess.server.entity.Game;
import com.chess.server.entity.GameMove;
import lombok.Getter;

@Getter
public class GameStateResponse {

    private Long gameId;
    private String whitePlayer;     // 백 플레이어 닉네임
    private String blackPlayer;     // 흑 플레이어 닉네임
    private String currentTurn;     // 현재 턴
    private String status;          // 게임 상태
    private String fromSquare;      // 마지막 이동 출발 위치
    private String toSquare;        // 마지막 이동 도착 위치
    private int moveNumber;         // 현재 수

    // 게임 상태 + 마지막 착수 정보 변환
    public GameStateResponse(Game game, GameMove lastMove) {
        this.gameId = game.getId();
        this.whitePlayer = game.getWhitePlayer().getNickname();
        this.blackPlayer = game.getBlackPlayer().getNickname();
        this.currentTurn = game.getCurrentTurn().name();
        this.status = game.getStatus().name();
        if (lastMove != null) {
            this.fromSquare = lastMove.getFromSquare();
            this.toSquare = lastMove.getToSquare();
            this.moveNumber = lastMove.getMoveNumber();
        }
    }
}