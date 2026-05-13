package com.chess.server.dto.response;

import com.chess.server.entity.Board;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class BoardDetailResponse {

    private Long id;
    private String title;               // 게시글 제목
    private String content;             // 게시글 내용
    private String authorNickname;      // 작성자 닉네임
    private int viewCount;              // 조회수
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    // 게임 기보 첨부 정보
    private Long recordId;              // 기보 ID
    private String recordTitle;         // 기보 제목
    private Integer startMoveNumber;    // 시작 수

    // 임의 포지션 첨부 정보
    private String fen;                 // FEN 문자열

    // Entity → DTO 변환
    public BoardDetailResponse(Board board) {
        this.id = board.getId();
        this.title = board.getTitle();
        this.content = board.getContent();
        this.authorNickname = board.getAuthor().getNickname();
        this.viewCount = board.getViewCount();
        this.createdAt = board.getCreatedAt();
        this.updatedAt = board.getUpdatedAt();

        // 게임 기보 첨부 시
        if (board.getGameRecord() != null) {
            this.recordId = board.getGameRecord().getId();
            this.recordTitle = board.getGameRecord().getTitle();
            this.startMoveNumber = board.getStartMoveNumber();
        }

        // 임의 포지션 첨부 시
        this.fen = board.getFen();
    }
}