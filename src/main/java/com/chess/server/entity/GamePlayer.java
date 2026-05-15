package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_players")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GamePlayer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom gameRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlayerStatus status = PlayerStatus.WAITING;

    @Enumerated(EnumType.STRING)
    @Column
    private PieceColor pieceColor;

    // 플레이어/관전자 역할 구분
    // PLAYER: 대기열, SPECTATOR: 관전석
    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlayerRole role = PlayerRole.PLAYER;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();

    public enum PlayerStatus {
        WAITING, READY
    }

    public enum PieceColor {
        WHITE, BLACK
    }

    public enum PlayerRole {
        PLAYER, SPECTATOR
    }

    public void ready() {
        this.status = PlayerStatus.READY;
    }

    public void cancelReady() {
        this.status = PlayerStatus.WAITING;
    }

    public void assignColor(PieceColor color) {
        this.pieceColor = color;
    }

    // 관전자로 역할 변경
    public void switchToSpectator() {
        this.role = PlayerRole.SPECTATOR;
        this.status = PlayerStatus.WAITING;  // 준비 상태 초기화
    }

    // 플레이어로 역할 변경
    public void switchToPlayer() {
        this.role = PlayerRole.PLAYER;
    }
}