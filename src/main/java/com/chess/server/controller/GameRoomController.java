package com.chess.server.controller;

import com.chess.server.dto.request.CreateRoomRequest;
import com.chess.server.dto.response.GameRoomResponse;
import com.chess.server.service.GameRoomService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;

    // 방 생성
    @PostMapping
    public ResponseEntity<GameRoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        GameRoomResponse response = gameRoomService.createRoom(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 대기중인 방 목록 조회 (로비)
    @GetMapping
    public ResponseEntity<List<GameRoomResponse>> getRoomList() {
        return ResponseEntity.ok(gameRoomService.getRoomList());
    }

    // 방 입장
    @PostMapping("/{roomId}/join")
    public ResponseEntity<GameRoomResponse> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        GameRoomResponse response = gameRoomService.joinRoom(roomId, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 방 퇴장
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        gameRoomService.leaveRoom(roomId, userDetails.getUsername());
        return ResponseEntity.ok("퇴장 완료");
    }
}