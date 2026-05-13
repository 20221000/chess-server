package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "boards")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Board {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 100)
    private String title;           // 게시글 제목

    @Column(nullable = false, columnDefinition = "TEXT")
    private String content;         // 게시글 내용

    // 작성자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private User author;

    // 게임 기보 첨부 (선택사항)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "record_id", nullable = true)
    private GameRecord gameRecord;

    @Column(nullable = true)
    private Integer startMoveNumber;    // 기보 시작 수 (null이면 처음부터)

    @Column(nullable = true, columnDefinition = "TEXT")
    private String fen;                 // 임의 포지션 FEN 문자열

    @Column(nullable = false)
    @Builder.Default
    private int viewCount = 0;          // 조회수

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column(nullable = false)
    private LocalDateTime updatedAt;

    // 제목 수정
    public void updateTitle(String title) {
        this.title = title;
    }

    // 내용 수정
    public void updateContent(String content) {
        this.content = content;
    }

    // 게임 기보 수정
    public void updateGameRecord(GameRecord gameRecord, Integer startMoveNumber) {
        this.gameRecord = gameRecord;
        this.startMoveNumber = startMoveNumber;
        this.fen = null;    // 기보 첨부 시 FEN 초기화
    }

    // 임의 포지션 수정
    public void updateFen(String fen) {
        this.fen = fen;
        this.gameRecord = null;         // FEN 첨부 시 기보 초기화
        this.startMoveNumber = null;
    }

    // 첨부 제거
    public void removeAttachment() {
        this.gameRecord = null;
        this.startMoveNumber = null;
        this.fen = null;
    }

    // 조회수 증가
    public void increaseViewCount() {
        this.viewCount++;
    }
}