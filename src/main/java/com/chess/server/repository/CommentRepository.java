package com.chess.server.repository;

import com.chess.server.entity.Board;
import com.chess.server.entity.Comment;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {

    // 게시글의 댓글 목록 조회 (오래된 순)
    List<Comment> findByBoardOrderByCreatedAtAsc(Board board);
}