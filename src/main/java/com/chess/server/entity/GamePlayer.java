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

    // 어느 방의 플레이어인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "room_id", nullable = false)
    private GameRoom gameRoom;

    // 어느 유저인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private PlayerStatus status = PlayerStatus.WAITING;  // 준비 상태

    @Enumerated(EnumType.STRING)
    @Column
    private PieceColor pieceColor;  // 기물 색상 (게임 시작 시 배정)

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime joinedAt = LocalDateTime.now();  // 입장 시간

    // 플레이어 상태 (WAITING: 대기, READY: 준비완료)
    public enum PlayerStatus {
        WAITING, READY
    }

    // 기물 색상 (WHITE: 백, BLACK: 흑)
    public enum PieceColor {
        WHITE, BLACK
    }

    // 준비 상태로 변경
    public void ready() {
        this.status = PlayerStatus.READY;
    }

    // 대기 상태로 변경
    public void cancelReady() {
        this.status = PlayerStatus.WAITING;
    }
}