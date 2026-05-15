package com.chess.server.controller;

import com.chess.server.dto.request.CreateRoomRequest;
import com.chess.server.dto.response.GameRoomResponse;
import com.chess.server.dto.response.RoomStateResponse;
import com.chess.server.service.GameRoomService;
import com.chess.server.service.GameService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/rooms")
@RequiredArgsConstructor
public class GameRoomController {

    private final GameRoomService gameRoomService;
    private final GameService gameService;
    private final SimpMessagingTemplate messagingTemplate;

    // 방 생성
    @PostMapping
    public ResponseEntity<GameRoomResponse> createRoom(
            @Valid @RequestBody CreateRoomRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        GameRoomResponse response = gameRoomService.createRoom(request, userDetails.getUsername());
        return ResponseEntity.ok(response);
    }

    // 대기중인 방 목록 조회
    @GetMapping
    public ResponseEntity<List<GameRoomResponse>> getRoomList() {
        return ResponseEntity.ok(gameRoomService.getRoomList());
    }

    // 방 입장 → WebSocket으로 최신 방 상태 브로드캐스트
    @PostMapping("/{roomId}/join")
    public ResponseEntity<GameRoomResponse> joinRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        GameRoomResponse response = gameRoomService.joinRoom(roomId, userDetails.getUsername());
        RoomStateResponse roomState = gameRoomService.getRoomState(roomId);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok(response);
    }

    // 방 퇴장
    // 게임 중 퇴장 시 → ABANDONED 처리 후 gameState/roomState 브로드캐스트
    @DeleteMapping("/{roomId}/leave")
    public ResponseEntity<String> leaveRoom(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        boolean wasPlaying = gameRoomService.leaveRoom(roomId, userDetails.getUsername());

        if (wasPlaying) {
            // ABANDONED 상태 gameState 브로드캐스트
            gameService.getAbandonedGameState(roomId).ifPresent(gameState ->
                    messagingTemplate.convertAndSend("/topic/game/" + roomId, gameState)
            );
            // 방 상태 브로드캐스트 (방에 사람이 남아있을 때만)
            try {
                RoomStateResponse roomState = gameRoomService.getRoomState(roomId);
                messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
            } catch (Exception ignored) {}
        }

        return ResponseEntity.ok("퇴장 완료");
    }

    // 준비 → WebSocket으로 최신 방 상태 브로드캐스트
    @PostMapping("/{roomId}/ready")
    public ResponseEntity<String> ready(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        RoomStateResponse roomState = gameRoomService.ready(roomId, userDetails.getUsername());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok("준비 완료");
    }

    // 준비 취소 → WebSocket으로 최신 방 상태 브로드캐스트
    @PostMapping("/{roomId}/cancel-ready")
    public ResponseEntity<String> cancelReady(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        RoomStateResponse roomState = gameRoomService.cancelReady(roomId, userDetails.getUsername());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok("준비 취소");
    }

    // 방 단건 조회
    @GetMapping("/{roomId}")
    public ResponseEntity<RoomStateResponse> getRoom(
            @PathVariable Long roomId) {

        RoomStateResponse response = gameRoomService.getRoomState(roomId);
        return ResponseEntity.ok(response);
    }

    // 관전자 → 대기열
    @PostMapping("/{roomId}/switch-to-player")
    public ResponseEntity<String> switchToPlayer(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        RoomStateResponse roomState = gameRoomService.switchToPlayer(roomId, userDetails.getUsername());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok("대기열로 이동");
    }

    // 대기열 → 관전석
    @PostMapping("/{roomId}/switch-to-spectator")
    public ResponseEntity<String> switchToSpectator(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        RoomStateResponse roomState = gameRoomService.switchToSpectator(roomId, userDetails.getUsername());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok("관전석으로 이동");
    }

    // 다시하기 → WebSocket으로 최신 방 상태 브로드캐스트
    @PostMapping("/{roomId}/restart")
    public ResponseEntity<String> restartGame(
            @PathVariable Long roomId,
            @AuthenticationPrincipal UserDetails userDetails) {

        RoomStateResponse roomState = gameRoomService.restartGame(roomId, userDetails.getUsername());
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok("다시하기");
    }

    // 방장 색상 선택 (WHITE/BLACK/RANDOM)
    @PostMapping("/{roomId}/host-color")
    public ResponseEntity<String> updateHostColor(
            @PathVariable Long roomId,
            @RequestParam String color,
            @AuthenticationPrincipal UserDetails userDetails) {

        RoomStateResponse roomState = gameRoomService.updateHostColor(roomId, userDetails.getUsername(), color);
        messagingTemplate.convertAndSend("/topic/room/" + roomId, roomState);
        return ResponseEntity.ok("색상 변경 완료");
    }
}