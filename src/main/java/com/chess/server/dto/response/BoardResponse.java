package com.chess.server.dto.response;

import com.chess.server.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardResponse {

    private Long id;
    private String title;           // 게시글 제목
    private String authorNickname;  // 작성자 닉네임
    private int viewCount;          // 조회수
    private boolean hasAttachment;  // 기보 첨부 여부
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // Entity → DTO 변환
    public BoardResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.authorNickname = board.getAuthor().getNickname();
        this.viewCount = board.getViewCount();
        this.hasAttachment = board.getGameRecord() != null || board.getFen() != null;
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();
    }
}