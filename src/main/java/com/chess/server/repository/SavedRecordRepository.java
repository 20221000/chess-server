package com.chess.server.repository;

import com.chess.server.entity.GameRecord;
import com.chess.server.entity.SavedRecord;
import com.chess.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface SavedRecordRepository extends JpaRepository<SavedRecord, Long> {

    // 저장한 기보 목록 조회
    List<SavedRecord> findByUserOrderBySavedAtDesc(User user);

    // 이미 저장한 기보인지 확인
    boolean existsByUserAndGameRecord(User user, GameRecord gameRecord);

    // 저장한 기보 조회 (삭제 시 사용)
    Optional<SavedRecord> findByUserAndGameRecord(User user, GameRecord gameRecord);
}