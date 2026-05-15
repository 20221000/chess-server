package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(
        name = "game_records",
        // game_id 단독 unique → (game_id, owner_id) 조합 unique로 변경
        // 한 게임에 두 플레이어 각각 기보 저장 가능
        uniqueConstraints = @UniqueConstraint(
                name = "UK_game_records_game_owner",
                columnNames = {"game_id", "owner_id"}
        )
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class GameRecord {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // 어느 게임의 기보인지 (한 게임에 여러 플레이어의 기보 저장 가능)
    // @OneToOne → @ManyToOne: 한 게임에 white/black 각각 기보를 가질 수 있음
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "game_id", nullable = false)
    private Game game;

    // 기보 소유자
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    private User owner;

    @Column(length = 100)
    private String title;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private RecordVisibility visibility = RecordVisibility.PUBLIC;

    @CreatedDate
    @Column(nullable = false, updatable = false)
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum RecordVisibility {
        PUBLIC, PRIVATE
    }

    public void updateTitle(String title) {
        this.title = title;
    }

    public void updateVisibility(RecordVisibility visibility) {
        this.visibility = visibility;
    }
}