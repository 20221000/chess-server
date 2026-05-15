package com.chess.server.dto.response;

import com.chess.server.entity.Game;
import com.chess.server.entity.GameMove;
import lombok.Getter;

@Getter
public class GameStateResponse {

    private Long gameId;
    private Long roomId;            // roomId 기반 WebSocket 브로드캐스트용
    private String whitePlayer;
    private String blackPlayer;
    private String whitePlayerNickname;
    private String blackPlayerNickname;
    private String currentTurn;
    private String status;
    private String result;
    private String fromSquare;
    private String toSquare;
    private String promotion;
    private int moveNumber;

    public GameStateResponse(Game game, GameMove lastMove) {
        this.gameId = game.getId();
        // 솔로 게임은 gameRoom이 null
        this.roomId = game.getGameRoom() != null ? game.getGameRoom().getId() : null;
        this.whitePlayer = game.getWhitePlayer().getUsername();
        this.blackPlayer = game.getBlackPlayer().getUsername();
        this.whitePlayerNickname = game.getWhitePlayer().getNickname();
        this.blackPlayerNickname = game.getBlackPlayer().getNickname();
        this.currentTurn = game.getCurrentTurn().name();
        this.status = game.getStatus().name();
        this.result = game.getResult() != null ? game.getResult().name() : null;
        if (lastMove != null) {
            this.fromSquare = lastMove.getFromSquare();
            this.toSquare = lastMove.getToSquare();
            this.promotion = lastMove.getPromotion();
            this.moveNumber = lastMove.getMoveNumber();
        }
    }
}