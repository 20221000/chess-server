package com.chess.server.service;

import com.chess.server.dto.request.CreateBoardRequest;
import com.chess.server.dto.request.UpdateBoardRequest;
import com.chess.server.dto.response.BoardDetailResponse;
import com.chess.server.dto.response.BoardResponse;
import com.chess.server.entity.Board;
import com.chess.server.entity.GameRecord;
import com.chess.server.entity.User;
import com.chess.server.exception.BadRequestException;
import com.chess.server.exception.NotFoundException;
import com.chess.server.repository.BoardRepository;
import com.chess.server.repository.GameRecordRepository;
import com.chess.server.repository.SavedRecordRepository;
import com.chess.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class BoardService {

    private final BoardRepository boardRepository;
    private final UserRepository userRepository;
    private final GameRecordRepository gameRecordRepository;
    private final SavedRecordRepository savedRecordRepository;

    // 게시글 작성
    @Transactional
    public BoardDetailResponse createBoard(CreateBoardRequest request, String username) {
        User author = findUserByUsername(username);

        // 기보/FEN 동시 첨부 불가
        if (request.getRecordId() != null && request.getFen() != null) {
            throw new BadRequestException("기보와 FEN을 동시에 첨부할 수 없습니다.");
        }

        Board.BoardBuilder builder = Board.builder()
                .title(request.getTitle())
                .content(request.getContent())
                .author(author);

        // 게임 기보 첨부
        if (request.getRecordId() != null) {
            GameRecord gameRecord = findGameRecord(request.getRecordId());
            validateGameRecordAccess(gameRecord, author);
            builder.gameRecord(gameRecord)
                    .startMoveNumber(request.getStartMoveNumber());
        }

        // 임의 포지션 첨부
        if (request.getFen() != null) {
            builder.fen(request.getFen());
        }

        return new BoardDetailResponse(boardRepository.save(builder.build()));
    }

    // 게시글 목록 조회 (페이지네이션)
    @Transactional(readOnly = true)
    public Page<BoardResponse> getBoardList(int page, int size) {
        Pageable pageable = PageRequest.of(page, size);
        return boardRepository.findAllByOrderByCreatedAtDesc(pageable)
                .map(BoardResponse::new);
    }

    // 게시글 상세 조회 (조회수 증가)
    @Transactional
    public BoardDetailResponse getBoard(Long boardId) {
        Board board = findBoard(boardId);
        board.increaseViewCount();
        return new BoardDetailResponse(board);
    }

    // 게시글 수정
    @Transactional
    public BoardDetailResponse updateBoard(Long boardId, UpdateBoardRequest request, String username) {
        Board board = findBoard(boardId);
        User user = findUserByUsername(username);

        // 작성자 본인 확인
        validateAuthor(board, user);

        // 기보/FEN 동시 첨부 불가
        if (request.getRecordId() != null && request.getFen() != null) {
            throw new BadRequestException("기보와 FEN을 동시에 첨부할 수 없습니다.");
        }

        if (request.getTitle() != null) board.updateTitle(request.getTitle());
        if (request.getContent() != null) board.updateContent(request.getContent());

        // 첨부 제거
        if (request.isRemoveAttachment()) {
            board.removeAttachment();
        }
        // 게임 기보 수정
        else if (request.getRecordId() != null) {
            GameRecord gameRecord = findGameRecord(request.getRecordId());
            validateGameRecordAccess(gameRecord, user);
            board.updateGameRecord(gameRecord, request.getStartMoveNumber());
        }
        // 임의 포지션 수정
        else if (request.getFen() != null) {
            board.updateFen(request.getFen());
        }

        return new BoardDetailResponse(board);
    }

    // 게시글 삭제
    @Transactional
    public void deleteBoard(Long boardId, String username) {
        Board board = findBoard(boardId);
        User user = findUserByUsername(username);

        // 작성자 본인 확인
        validateAuthor(board, user);

        boardRepository.delete(board);
    }

    // 게시글 조회 (내부 공통 메서드)
    private Board findBoard(Long boardId) {
        return boardRepository.findById(boardId)
                .orElseThrow(() -> new NotFoundException("게시글을 찾을 수 없습니다."));
    }

    // 유저 조회 (내부 공통 메서드)
    private User findUserByUsername(String username) {
        return userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("존재하지 않는 유저입니다."));
    }

    // 기보 조회 (내부 공통 메서드)
    private GameRecord findGameRecord(Long recordId) {
        return gameRecordRepository.findById(recordId)
                .orElseThrow(() -> new NotFoundException("기보를 찾을 수 없습니다."));
    }

    // 작성자 본인 확인 (내부 공통 메서드)
    private void validateAuthor(Board board, User user) {
        if (!board.getAuthor().getId().equals(user.getId())) {
            throw new BadRequestException("본인의 게시글만 수정/삭제할 수 있습니다.");
        }
    }

    // 기보 접근 권한 확인 - 본인 소유 또는 저장한 기보 (내부 공통 메서드)
    private void validateGameRecordAccess(GameRecord gameRecord, User user) {
        boolean isOwner = gameRecord.getOwner().getId().equals(user.getId());
        boolean isSaved = savedRecordRepository.existsByUserAndGameRecord(user, gameRecord);

        if (!isOwner && !isSaved) {
            throw new BadRequestException("본인의 기보 또는 저장한 기보만 첨부할 수 있습니다.");
        }
    }
}