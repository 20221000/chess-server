package com.chess.server.dto.response;

import com.chess.server.entity.GameRecord;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class GameRecordResponse {

    private Long id;
    private Long gameId;
    private String ownerNickname;   // 기보 소유자 닉네임
    private String title;           // 기보 제목
    private String visibility;      // 공개 여부
    private LocalDateTime createdAt;

    public GameRecordResponse(GameRecord gameRecord) {
        this.id = gameRecord.getId();
        this.gameId = gameRecord.getGame().getId();
        this.ownerNickname = gameRecord.getOwner().getNickname();
        this.title = gameRecord.getTitle();
        this.visibility = gameRecord.getVisibility().name();
        this.createdAt = gameRecord.getCreatedAt();
    }
}