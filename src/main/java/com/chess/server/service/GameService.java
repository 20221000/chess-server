package com.chess.server.service;

import com.chess.server.dto.request.GameMoveRequest;
import com.chess.server.dto.response.GameStateResponse;
import com.chess.server.entity.*;
import com.chess.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameMoveRepository gameMoveRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final UserRepository userRepository;

    // 게임 시작 (2명 모두 준비 시 호출)
    @Transactional
    public GameStateResponse startGame(Long roomId) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        // 준비 완료 플레이어 확인
        List<GamePlayer> readyPlayers = gamePlayerRepository
                .findByGameRoom(gameRoom)
                .stream()
                .filter(p -> p.getStatus() == GamePlayer.PlayerStatus.READY)
                .toList();

        if (readyPlayers.size() < 2) {
            throw new IllegalArgumentException("2명이 준비해야 게임을 시작할 수 있습니다.");
        }

        // 랜덤으로 흑백 배정
        User whitePlayer = readyPlayers.get(0).getUser();
        User blackPlayer = readyPlayers.get(1).getUser();

        Game game = Game.builder()
                .gameRoom(gameRoom)
                .whitePlayer(whitePlayer)
                .blackPlayer(blackPlayer)
                .currentTurn(GamePlayer.PieceColor.WHITE)
                .build();

        gameRepository.save(game);

        // 방 상태 변경
        gameRoom.startGame();
        gameRoomRepository.save(gameRoom);

        return new GameStateResponse(game, null);
    }

    // 착수 처리
    @Transactional
    public GameStateResponse makeMove(GameMoveRequest request, String username) {

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("게임을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 현재 턴 플레이어 확인
        validateTurn(game, user);

        // 착수 기록 저장
        int moveNumber = gameMoveRepository.countByGame(game) + 1;

        GameMove gameMove = GameMove.builder()
                .game(game)
                .user(user)
                .fromSquare(request.getFromSquare())
                .toSquare(request.getToSquare())
                .promotion(request.getPromotion())
                .moveNumber(moveNumber)
                .pieceColor(game.getCurrentTurn())
                .build();

        gameMoveRepository.save(gameMove);

        // 턴 변경
        game.changeTurn();
        gameRepository.save(game);

        return new GameStateResponse(game, gameMove);
    }

    // 게임 종료 처리
    @Transactional
    public void finishGame(Long gameId, Game.GameResult result) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("게임을 찾을 수 없습니다."));

        game.finish(result);
        gameRepository.save(game);

        // 방 상태 대기중으로 변경
        game.getGameRoom().waitGame();
        gameRoomRepository.save(game.getGameRoom());
    }

    // 현재 턴 플레이어인지 검증
    private void validateTurn(Game game, User user) {
        boolean isWhiteTurn = game.getCurrentTurn() == GamePlayer.PieceColor.WHITE
                && game.getWhitePlayer().getId().equals(user.getId());
        boolean isBlackTurn = game.getCurrentTurn() == GamePlayer.PieceColor.BLACK
                && game.getBlackPlayer().getId().equals(user.getId());

        if (!isWhiteTurn && !isBlackTurn) {
            throw new IllegalArgumentException("현재 본인의 턴이 아닙니다.");
        }
    }
}