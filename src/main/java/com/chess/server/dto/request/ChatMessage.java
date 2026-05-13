package com.chess.server.dto.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatMessage {

    private Long roomId;        // 방 ID
    private String sender;      // 보낸 사람 닉네임
    private String content;     // 채팅 내용
    private MessageType type;   // 메시지 타입

    // 메시지 타입 (CHAT: 채팅, ENTER: 입장, LEAVE: 퇴장)
    public enum MessageType {
        CHAT, ENTER, LEAVE
    }
}