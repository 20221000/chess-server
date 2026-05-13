package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "games")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class Game {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 방의 게임인지 나와의 대전인지 파악
    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    private GameRoom gameRoom;

    // 백 기물 플레이어
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_player_id", nullable = false)
    private User whitePlayer;

    // 흑 기물 플레이어
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_player_id", nullable = false)
    private User blackPlayer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;    // 게임 상태

    @Enumerated(EnumType.STRING)
    @Column
    private GameResult result;      // 게임 결과

    @Enumerated(EnumType.STRING)
    @Column
    private GamePlayer.PieceColor currentTurn;  // 현재 턴 (WHITE/BLACK)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();  // 게임 시작 시간

    @Column
    private LocalDateTime endedAt;  // 게임 종료 시간

    // 게임 상태 (IN_PROGRESS: 진행중, FINISHED: 종료, ABANDONED: 중단)
    public enum GameStatus {
        IN_PROGRESS, FINISHED, ABANDONED
    }

    // 게임 결과 (WHITE_WIN: 백 승, BLACK_WIN: 흑 승, DRAW: 무승부)
    public enum GameResult {
        WHITE_WIN, BLACK_WIN, DRAW
    }

    // 게임 종료 처리
    public void finish(GameResult result) {
        this.status = GameStatus.FINISHED;
        this.result = result;
        this.endedAt = LocalDateTime.now();
    }

    // 턴 변경
    public void changeTurn() {
        this.currentTurn = (this.currentTurn == GamePlayer.PieceColor.WHITE)
                ? GamePlayer.PieceColor.BLACK
                : GamePlayer.PieceColor.WHITE;
    }
}