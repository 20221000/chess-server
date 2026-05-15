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
    private String title;

    @Column(nullable = false)
    private int maxPlayers;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RoomStatus status = RoomStatus.WAITING;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "host_id", nullable = false)
    private User host;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private HostColor hostColor = HostColor.RANDOM;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RoomStatus {
        WAITING, PLAYING, CLOSED
    }

    public enum HostColor {
        WHITE, BLACK, RANDOM
    }

    public void close() {
        this.status = RoomStatus.CLOSED;
    }

    public void startGame() {
        this.status = RoomStatus.PLAYING;
    }

    public void waitGame() {
        this.status = RoomStatus.WAITING;
    }

    public void updateHostColor(HostColor hostColor) {
        this.hostColor = hostColor;
    }

    // 방장 이양
    public void updateHost(User newHost) {
        this.host = newHost;
    }
}