package com.chess.server.controller;

import com.chess.server.dto.request.CreateBoardRequest;
import com.chess.server.dto.request.CreateCommentRequest;
import com.chess.server.dto.request.UpdateBoardRequest;
import com.chess.server.dto.request.UpdateCommentRequest;
import com.chess.server.dto.response.BoardDetailResponse;
import com.chess.server.dto.response.BoardResponse;
import com.chess.server.dto.response.CommentResponse;
import com.chess.server.service.BoardService;
import com.chess.server.service.CommentService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/boards")
@RequiredArgsConstructor
public class BoardController {

    private final BoardService boardService;
    private final CommentService commentService;

    // 게시글 작성
    @PostMapping
    public ResponseEntity<BoardDetailResponse> createBoard(
            @Valid @RequestBody CreateBoardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(boardService.createBoard(request, userDetails.getUsername()));
    }

    // 게시글 목록 조회
    @GetMapping
    public ResponseEntity<Page<BoardResponse>> getBoardList(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(boardService.getBoardList(page, size));
    }

    // 게시글 상세 조회
    @GetMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> getBoard(
            @PathVariable Long boardId) {
        return ResponseEntity.ok(boardService.getBoard(boardId));
    }

    // 게시글 수정
    @PatchMapping("/{boardId}")
    public ResponseEntity<BoardDetailResponse> updateBoard(
            @PathVariable Long boardId,
            @Valid @RequestBody UpdateBoardRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(boardService.updateBoard(boardId, request, userDetails.getUsername()));
    }

    // 게시글 삭제
    @DeleteMapping("/{boardId}")
    public ResponseEntity<Void> deleteBoard(
            @PathVariable Long boardId,
            @AuthenticationPrincipal UserDetails userDetails) {
        boardService.deleteBoard(boardId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }

    // 댓글 작성
    @PostMapping("/{boardId}/comments")
    public ResponseEntity<CommentResponse> createComment(
            @PathVariable Long boardId,
            @Valid @RequestBody CreateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.createComment(boardId, request, userDetails.getUsername()));
    }

    // 댓글 목록 조회
    @GetMapping("/{boardId}/comments")
    public ResponseEntity<List<CommentResponse>> getComments(
            @PathVariable Long boardId) {
        return ResponseEntity.ok(commentService.getComments(boardId));
    }

    // 댓글 수정
    @PatchMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<CommentResponse> updateComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @Valid @RequestBody UpdateCommentRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(commentService.updateComment(boardId, commentId, request, userDetails.getUsername()));
    }

    // 댓글 삭제
    @DeleteMapping("/{boardId}/comments/{commentId}")
    public ResponseEntity<Void> deleteComment(
            @PathVariable Long boardId,
            @PathVariable Long commentId,
            @AuthenticationPrincipal UserDetails userDetails) {
        commentService.deleteComment(boardId, commentId, userDetails.getUsername());
        return ResponseEntity.ok().build();
    }
}