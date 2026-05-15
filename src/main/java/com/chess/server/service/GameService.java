package com.chess.server.service;

import com.chess.server.dto.request.GameFinishRequest;
import com.chess.server.dto.request.GameMoveRequest;
import com.chess.server.dto.response.GameStateResponse;
import com.chess.server.entity.*;
import com.chess.server.exception.BadRequestException;
import com.chess.server.exception.NotFoundException;
import com.chess.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;

@Service
@RequiredArgsConstructor
public class GameService {

    private final GameRepository gameRepository;
    private final GameMoveRepository gameMoveRepository;
    private final GameRoomRepository gameRoomRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final UserRepository userRepository;
    private final GameRecordSaveService gameRecordSaveService;

    // 게임 시작 → 항상 새 게임 생성 (기보 분리)
    @Transactional
    public GameStateResponse startGame(Long roomId) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        // 이미 진행 중인 게임이 있으면 그대로 반환
        if (gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING) {
            Game existingGame = gameRepository
                    .findByGameRoomAndStatus(gameRoom, Game.GameStatus.IN_PROGRESS)
                    .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다."));
            return new GameStateResponse(existingGame, null);
        }

        List<GamePlayer> readyPlayers = gamePlayerRepository
                .findByGameRoom(gameRoom)
                .stream()
                .filter(p -> p.getStatus() == GamePlayer.PlayerStatus.READY
                        && p.getRole() == GamePlayer.PlayerRole.PLAYER)
                .toList();

        if (readyPlayers.size() < 2) {
            throw new BadRequestException("2명이 준비해야 게임을 시작할 수 있습니다.");
        }

        // 방장/비방장 구분
        Optional<GamePlayer> hostPlayerOpt = readyPlayers.stream()
                .filter(p -> p.getUser().getId().equals(gameRoom.getHost().getId()))
                .findFirst();

        GamePlayer hostPlayer = hostPlayerOpt.orElse(readyPlayers.get(0));
        GamePlayer guestPlayer = readyPlayers.stream()
                .filter(p -> !p.getUser().getId().equals(hostPlayer.getUser().getId()))
                .findFirst()
                .orElse(readyPlayers.get(1));

        // hostColor 기반 색상 배정
        User whitePlayer;
        User blackPlayer;

        GameRoom.HostColor hostColor = gameRoom.getHostColor();
        if (hostColor == GameRoom.HostColor.WHITE) {
            whitePlayer = hostPlayer.getUser();
            blackPlayer = guestPlayer.getUser();
        } else if (hostColor == GameRoom.HostColor.BLACK) {
            whitePlayer = guestPlayer.getUser();
            blackPlayer = hostPlayer.getUser();
        } else {
            if (Math.random() < 0.5) {
                whitePlayer = hostPlayer.getUser();
                blackPlayer = guestPlayer.getUser();
            } else {
                whitePlayer = guestPlayer.getUser();
                blackPlayer = hostPlayer.getUser();
            }
        }

        // 항상 새 게임 생성 → 기보 섞임 방지
        Game game = Game.builder()
                .gameRoom(gameRoom)
                .whitePlayer(whitePlayer)
                .blackPlayer(blackPlayer)
                .currentTurn(GamePlayer.PieceColor.WHITE)
                .build();

        gameRepository.save(game);

        // GamePlayer에 색상 배정
        readyPlayers.forEach(p -> {
            if (p.getUser().getId().equals(whitePlayer.getId())) {
                p.assignColor(GamePlayer.PieceColor.WHITE);
            } else {
                p.assignColor(GamePlayer.PieceColor.BLACK);
            }
        });
        gamePlayerRepository.saveAll(readyPlayers);

        gameRoom.startGame();
        gameRoomRepository.save(gameRoom);

        return new GameStateResponse(game, null);
    }

    // 착수 처리
    @Transactional
    public GameStateResponse makeMove(GameMoveRequest request, String username) {

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        validateTurn(game, user);

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

        game.changeTurn();
        gameRepository.save(game);

        return new GameStateResponse(game, gameMove);
    }

    // 게임 종료 처리
    @Transactional
    public GameStateResponse finishGame(GameFinishRequest request, String username) {

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        boolean isParticipant =
                game.getWhitePlayer().getId().equals(user.getId()) ||
                        game.getBlackPlayer().getId().equals(user.getId());

        if (!isParticipant) {
            throw new BadRequestException("게임 참여자만 종료할 수 있습니다.");
        }

        if (game.getStatus() == Game.GameStatus.FINISHED) {
            return new GameStateResponse(game, null);
        }

        game.finish(request.getResult());
        gameRepository.save(game);

        game.getGameRoom().waitGame();
        gameRoomRepository.save(game.getGameRoom());

        return new GameStateResponse(game, null);
    }

    // 게임 중 퇴장 후 ABANDONED 상태 gameState 반환
    @Transactional(readOnly = true)
    public Optional<GameStateResponse> getAbandonedGameState(Long roomId) {
        return gameRoomRepository.findById(roomId)
                .flatMap(room -> gameRepository
                        .findByGameRoomAndStatus(room, Game.GameStatus.ABANDONED))
                .map(game -> {
                    game.getWhitePlayer().getNickname();
                    game.getBlackPlayer().getNickname();
                    return new GameStateResponse(game, null);
                });
    }

    // 게임 조회 (컨트롤러에서 기보 저장 시 사용)
    @Transactional(readOnly = true)
    public Game getGame(Long gameId) {
        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new NotFoundException("게임을 찾을 수 없습니다."));
        game.getWhitePlayer().getNickname();
        game.getBlackPlayer().getNickname();
        return game;
    }

    // 현재 턴 플레이어 검증
    private void validateTurn(Game game, User user) {
        boolean isWhiteTurn = game.getCurrentTurn() == GamePlayer.PieceColor.WHITE
                && game.getWhitePlayer().getId().equals(user.getId());
        boolean isBlackTurn = game.getCurrentTurn() == GamePlayer.PieceColor.BLACK
                && game.getBlackPlayer().getId().equals(user.getId());

        if (!isWhiteTurn && !isBlackTurn) {
            throw new BadRequestException("현재 본인의 턴이 아닙니다.");
        }
    }
}