package com.chess.server.controller;

import com.chess.server.dto.request.ChatMessage;
import com.chess.server.dto.request.GameMoveRequest;
import com.chess.server.dto.response.GameStateResponse;
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
    private final SimpMessagingTemplate messagingTemplate;

    // 착수 처리 (/app/game.move로 메시지 수신)
    @MessageMapping("/game.move")
    public void makeMove(@Payload GameMoveRequest request, Principal principal) {

        GameStateResponse response = gameService.makeMove(request, principal.getName());

        // 해당 방 구독자 전체에게 착수 결과 전송
        messagingTemplate.convertAndSend(
                "/topic/game/" + request.getGameId(),
                response
        );
    }

    // 게임 시작 (/app/game.start로 메시지 수신)
    @MessageMapping("/game.start")
    public void startGame(@Payload Long roomId) {

        GameStateResponse response = gameService.startGame(roomId);

        // 해당 방 구독자 전체에게 게임 시작 알림
        messagingTemplate.convertAndSend(
                "/topic/room/" + roomId,
                response
        );
    }

    // 채팅 (/app/chat.send로 메시지 수신)
    @MessageMapping("/chat.send")
    public void sendChat(@Payload ChatMessage message,
                         SimpMessageHeaderAccessor headerAccessor) {

        // 해당 방 구독자 전체에게 채팅 전송
        messagingTemplate.convertAndSend(
                "/topic/chat/" + message.getRoomId(),
                message
        );
    }

    // 입장 알림 (/app/chat.enter로 메시지 수신)
    @MessageMapping("/chat.enter")
    public void enterRoom(@Payload ChatMessage message,
                          SimpMessageHeaderAccessor headerAccessor) {

        message = ChatMessage.builder()
                .roomId(message.getRoomId())
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