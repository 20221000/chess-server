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

    private Long roomId;
    private String username;    // 내/상대 채팅 구분용
    private String sender;      // 보낸 사람 닉네임 (화면 표시용)
    private String content;
    private MessageType type;

    public enum MessageType {
        CHAT, ENTER, LEAVE
    }
}