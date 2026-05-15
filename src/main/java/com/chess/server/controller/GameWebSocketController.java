package com.chess.server.controller;

import com.chess.server.dto.request.ChatMessage;
import com.chess.server.dto.request.GameFinishRequest;
import com.chess.server.dto.request.GameMoveRequest;
import com.chess.server.dto.response.GameStateResponse;
import com.chess.server.entity.Game;
import com.chess.server.service.GameRecordSaveService;
import com.chess.server.service.GameService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.messaging.simp.SimpMessageHeaderAccessor;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.security.Principal;

@Controller
@RequiredArgsConstructor
public class GameWebSocketController {

    private final GameService gameService;
    private final GameRecordSaveService gameRecordSaveService;
    private final SimpMessagingTemplate messagingTemplate;

    // 착수 처리 → roomId 기반 브로드캐스트
    @MessageMapping("/game.move")
    public void makeMove(@Payload GameMoveRequest request, Principal principal) {

        GameStateResponse response = gameService.makeMove(request, principal.getName());

        messagingTemplate.convertAndSend(
                "/topic/game/" + response.getRoomId(),
                response
        );
    }

    // 게임 시작 → roomId 기반 브로드캐스트
    @MessageMapping("/game.start")
    public void startGame(@Payload Long roomId) {

        GameStateResponse response = gameService.startGame(roomId);

        messagingTemplate.convertAndSend(
                "/topic/game/" + roomId,
                response
        );
    }

    // 게임 종료 → roomId 기반 브로드캐스트
    @MessageMapping("/game.finish")
    public void finishGame(@Payload GameFinishRequest request, Principal principal) {

        GameStateResponse response = gameService.finishGame(request, principal.getName());

        Game game = gameService.getGame(request.getGameId());
        gameRecordSaveService.saveRecordForPlayer(game, game.getWhitePlayer());
        gameRecordSaveService.saveRecordForPlayer(game, game.getBlackPlayer());

        messagingTemplate.convertAndSend(
                "/topic/game/" + response.getRoomId(),
                response
        );
    }

    // 채팅
    @MessageMapping("/chat.send")
    public void sendChat(@Payload ChatMessage message,
                         SimpMessageHeaderAccessor headerAccessor) {

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                message
        );
    }

    // 입장 알림
    @MessageMapping("/chat.enter")
    public void enterRoom(@Payload ChatMessage message,
                          SimpMessageHeaderAccessor headerAccessor) {

        message = ChatMessage.builder()
                .roomId(message.getRoomId())
                .username(message.getUsername())
                .sender(message.getSender())
                .content(message.getSender() + "님이 입장하셨습니다.")
                .type(ChatMessage.MessageType.ENTER)
                .build();

        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                message
        );
    }
}