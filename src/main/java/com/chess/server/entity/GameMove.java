package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_moves")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GameMove {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 게임의 수인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // 누가 둔 수인지
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 10)
    private String fromSquare;  // 이동 출발 위치 (예: "e2")

    @Column(nullable = false, length = 10)
    private String toSquare;    // 이동 도착 위치 (예: "e4")

    @Column(length = 10)
    private String promotion;   // 프로모션 기물 (예: "Q", 해당 없으면 null)

    @Column(nullable = false)
    private int moveNumber;     // 몇 번째 수인지

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private GamePlayer.PieceColor pieceColor;   // 백/흑 구분

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime movedAt = LocalDateTime.now();    // 착수 시간
}