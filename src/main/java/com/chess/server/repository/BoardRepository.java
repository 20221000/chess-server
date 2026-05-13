package com.chess.server.repository;

import com.chess.server.entity.Board;
import com.chess.server.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface BoardRepository extends JpaRepository<Board, Long> {

    // 게시글 목록 조회 (최신순, 페이지네이션)
    Page<Board> findAllByOrderByCreatedAtDesc(Pageable pageable);

    // 특정 유저의 게시글 목록 조회
    Page<Board> findByAuthorOrderByCreatedAtDesc(User author, Pageable pageable);
}