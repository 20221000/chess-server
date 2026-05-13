package com.chess.server.service;

import com.chess.server.dto.request.GameMoveRequest;
import com.chess.server.dto.response.GameStateResponse;
import com.chess.server.entity.*;
import com.chess.server.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class SoloGameService {

    private final GameRepository gameRepository;
    private final GameMoveRepository gameMoveRepository;
    private final UserRepository userRepository;

    // 나와의 대전 시작
    @Transactional
    public GameStateResponse startSoloGame(String username) {

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 흑백 모두 같은 유저로 설정
        Game game = Game.builder()
                .whitePlayer(user)
                .blackPlayer(user)
                .currentTurn(GamePlayer.PieceColor.WHITE)
                .build();

        gameRepository.save(game);
        return new GameStateResponse(game, null);
    }

    // 착수 처리
    @Transactional
    public GameStateResponse makeMove(GameMoveRequest request, String username) {

        Game game = gameRepository.findById(request.getGameId())
                .orElseThrow(() -> new IllegalArgumentException("게임을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 나와의 대전은 턴 검증 없이 바로 착수
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

    // 나와의 대전 종료
    @Transactional
    public void finishSoloGame(Long gameId) {

        Game game = gameRepository.findById(gameId)
                .orElseThrow(() -> new IllegalArgumentException("게임을 찾을 수 없습니다."));

        // 나와의 대전은 결과 없이 종료
        game.finish(Game.GameResult.DRAW);
        gameRepository.save(game);
    }
}