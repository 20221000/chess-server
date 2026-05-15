package com.chess.server.service;

import com.chess.server.entity.Game;
import com.chess.server.entity.User;
import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class GameRecordSaveService {

    private final EntityManager entityManager;

    // REQUIRES_NEW: 부모 트랜잭션과 독립적으로 실행
    // INSERT IGNORE: 중복 시 DB 레벨에서 무시 → Hibernate 예외 없음
    // existsByGameAndOwner 체크 제거 → 두 플레이어 동시 호출 시 Race Condition 원인이었음
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void saveRecordForPlayer(Game game, User owner) {

        String whiteNick = game.getWhitePlayer().getNickname();
        String blackNick = game.getBlackPlayer().getNickname();
        String resultText = switch (game.getResult()) {
            case WHITE_WIN -> "백 승";
            case BLACK_WIN -> "흑 승";
            case DRAW -> "무승부";
        };

        String title = String.format("%s(백) vs %s(흑) - %s", whiteNick, blackNick, resultText);

        entityManager.createNativeQuery(
                        "INSERT IGNORE INTO game_records (created_at, game_id, owner_id, title, visibility) " +
                                "VALUES (:createdAt, :gameId, :ownerId, :title, :visibility)"
                )
                .setParameter("createdAt", LocalDateTime.now())
                .setParameter("gameId", game.getId())
                .setParameter("ownerId", owner.getId())
                .setParameter("title", title)
                .setParameter("visibility", "PUBLIC")
                .executeUpdate();
    }
}