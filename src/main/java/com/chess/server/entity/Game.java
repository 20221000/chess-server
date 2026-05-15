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

    // @OneToOne → @ManyToOne: 한 방에 여러 게임 가능 (다시하기)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = true)
    private GameRoom gameRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "white_player_id", nullable = false)
    private User whitePlayer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "black_player_id", nullable = false)
    private User blackPlayer;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private GameStatus status = GameStatus.IN_PROGRESS;

    @Enumerated(EnumType.STRING)
    @Column
    private GameResult result;

    @Enumerated(EnumType.STRING)
    @Column
    private GamePlayer.PieceColor currentTurn;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime startedAt = LocalDateTime.now();

    @Column
    private LocalDateTime endedAt;

    public enum GameStatus {
        IN_PROGRESS, FINISHED, ABANDONED
    }

    public enum GameResult {
        WHITE_WIN, BLACK_WIN, DRAW
    }

    public void finish(GameResult result) {
        this.status = GameStatus.FINISHED;
        this.result = result;
        this.endedAt = LocalDateTime.now();
    }

    public void abandon() {
        this.status = GameStatus.ABANDONED;
        this.result = null;
        this.endedAt = LocalDateTime.now();
    }

    public void changeTurn() {
        this.currentTurn = (this.currentTurn == GamePlayer.PieceColor.WHITE)
                ? GamePlayer.PieceColor.BLACK
                : GamePlayer.PieceColor.WHITE;
    }
}