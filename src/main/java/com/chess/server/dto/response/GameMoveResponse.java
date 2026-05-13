package com.chess.server.dto.response;

import com.chess.server.entity.GameMove;
import lombok.Getter;

@Getter
public class GameMoveResponse {

    private int moveNumber;         // 수 번호
    private String fromSquare;      // 출발 위치
    private String toSquare;        // 도착 위치
    private String pieceColor;      // 백/흑
    private String promotion;       // 프로모션 기물

    public GameMoveResponse(GameMove gameMove) {
        this.moveNumber = gameMove.getMoveNumber();
        this.fromSquare = gameMove.getFromSquare();
        this.toSquare = gameMove.getToSquare();
        this.pieceColor = gameMove.getPieceColor().name();
        this.promotion = gameMove.getPromotion();
    }
}