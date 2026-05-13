package com.chess.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class GameMoveRequest {

    @NotNull
    private Long gameId;            // 게임 ID

    @NotBlank
    private String fromSquare;      // 이동 출발 위치 (예: "e2")

    @NotBlank
    private String toSquare;        // 이동 도착 위치 (예: "e4")

    private String promotion;       // 프로모션 기물 (선택)
}