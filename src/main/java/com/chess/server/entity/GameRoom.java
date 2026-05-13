package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "game_rooms")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GameRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 50)
    private String title;           // 방 제목

    @Column(nullable = false)
    private int maxPlayers;         // 최대 인원 (방장이 설정)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.WAITING;  // 방 상태

    // 방 생성자 (방장)
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();  // 방 생성일

    // 방 상태 (WAITING: 대기중, PLAYING: 게임중, CLOSED: 종료)
    public enum RoomStatus {
        WAITING, PLAYING, CLOSED
    }

    // 방 닫기 (모든 플레이어 퇴장 시)
    public void close() {
        this.status = RoomStatus.CLOSED;
    }

    // 게임 시작 시 방 상태 변경
    public void startGame() {
        this.status = RoomStatus.PLAYING;
    }

    // 게임 종료 시 방 상태 대기중으로 변경
    public void waitGame() {
        this.status = RoomStatus.WAITING;
    }
}