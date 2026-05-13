package com.chess.server.service;

import com.chess.server.dto.request.CreateCommentRequest;
import com.chess.server.dto.request.UpdateCommentRequest;
import com.chess.server.dto.response.CommentResponse;
import com.chess.server.entity.Board;
import com.chess.server.entity.Comment;
import com.chess.server.entity.User;
import com.chess.server.exception.BadRequestException;
import com.chess.server.exception.NotFoundException;
import com.chess.server.repository.BoardRepository;
import com.chess.server.repository.CommentRepository;
import com.chess.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CommentService {

    private final CommentRepository commentRepository;
    private final BoardRepository boardRepository;
    private final UserRepository userRepository;

    // 댓글 작성
    @Transactional
    public CommentResponse createComment(Long boardId, CreateCommentRequest request, String username) {
        Board board = findBoard(boardId);
        User author = findUserByUsername(username);

        Comment comment = Comment.builder()
                .content(request.getContent())
                .author(author)
                .board(board)
                .build();

        return new CommentResponse(commentRepository.save(comment));
    }

    // 댓글 목록 조회
    @Transactional(readOnly = true)
    public List<CommentResponse> getComments(Long boardId) {
        Board board = findBoard(boardId);
        return commentRepository.findByBoardOrderByCreatedAtAsc(board)
                .stream()
                .map(CommentResponse::new)
                .collect(Collectors.toList());
    }

    // 댓글 수정
    @Transactional
    public CommentResponse updateComment(Long boardId, Long commentId, UpdateCommentRequest request, String username) {
        findBoard(boardId);
        Comment comment = findComment(commentId);
        User user = findUserByUsername(username);

        // 작성자 본인 확인
        validateAuthor(comment, user);

        comment.updateContent(request.getContent());
        return new CommentResponse(comment);
    }

    // 댓글 삭제
    @Transactional
    public void deleteComment(Long boardId, Long commentId, String username) {
        findBoard(boardId);
        Comment comment = findComment(commentId);
        User user = findUserByUsername(username);

        // 작성자 본인 확인
        validateAuthor(comment, user);

        commentRepository.delete(comment);
    }

    // 게시글 조회 (내부 공통 메서드)
    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
    }

    // 댓글 조회 (내부 공통 메서드)
    private Comment findComment(Long commentId) {
        return commentRepository.findById(commentId)
                .orElseThrow(() -> new NotFoundException("댓글을 찾을 수 없습니다."));
    }

    // 유저 조회 (내부 공통 메서드)
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
    }

    // 작성자 본인 확인 (내부 공통 메서드)
    private void validateAuthor(Comment comment, User user) {
        if (!comment.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("본인의 댓글만 수정/삭제할 수 있습니다.");
        }
    }
}