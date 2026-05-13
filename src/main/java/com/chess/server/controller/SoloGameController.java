package com.chess.server.controller;

import com.chess.server.dto.request.GameMoveRequest;
import com.chess.server.dto.response.GameStateResponse;
import com.chess.server.service.SoloGameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/solo")
@RequiredArgsConstructor
public class SoloGameController {

    private final SoloGameService soloGameService;

    // 나와의 대전 시작
    @PostMapping("/start")
    public ResponseEntity<GameStateResponse> startSoloGame(
            @AuthenticationPrincipal UserDetails userDetails) {

        GameStateResponse response = soloGameService.startSoloGame(userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 착수
    @PostMapping("/move")
    public ResponseEntity<GameStateResponse> makeMove(
            @Valid @RequestBody GameMoveRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        GameStateResponse response = soloGameService.makeMove(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 나와의 대전 종료
    @PostMapping("/{gameId}/finish")
    public ResponseEntity<String> finishSoloGame(@PathVariable Long gameId) {
        soloGameService.finishSoloGame(gameId);
        return ResponseEntity.ok("게임 종료");
    }
}