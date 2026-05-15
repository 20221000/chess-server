package com.chess.server.dto.request;

import com.chess.server.entity.Game;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameFinishRequest {

    @NotNull
    private Long gameId;            // 종료할 게임 ID

    @NotNull
    private Game.GameResult result; // WHITE_WIN | BLACK_WIN | DRAW
}