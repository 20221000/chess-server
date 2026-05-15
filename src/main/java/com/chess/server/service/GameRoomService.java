package com.chess.server.service;

import com.chess.server.dto.request.CreateRoomRequest;
import com.chess.server.dto.response.GameRoomResponse;
import com.chess.server.dto.response.RoomStateResponse;
import com.chess.server.entity.Game;
import com.chess.server.entity.GamePlayer;
import com.chess.server.entity.GameRoom;
import com.chess.server.entity.User;
import com.chess.server.exception.BadRequestException;
import com.chess.server.exception.DuplicateException;
import com.chess.server.exception.NotFoundException;
import com.chess.server.repository.GamePlayerRepository;
import com.chess.server.repository.GameRepository;
import com.chess.server.repository.GameRoomRepository;
import com.chess.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final GameRepository gameRepository;
    private final UserRepository userRepository;

    // 방 생성
    @Transactional
    public GameRoomResponse createRoom(CreateRoomRequest request, String username) {

        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        GameRoom gameRoom = GameRoom.builder()
                .title(request.getTitle())
                .maxPlayers(request.getMaxPlayers())
                .host(host)
                .build();

        gameRoomRepository.save(gameRoom);

        GamePlayer gamePlayer = GamePlayer.builder()
                .gameRoom(gameRoom)
                .user(host)
                .role(GamePlayer.PlayerRole.PLAYER)
                .build();

        gamePlayerRepository.save(gamePlayer);

        return new GameRoomResponse(gameRoom, 1);
    }

    // 대기중인 방 목록 조회
    @Transactional(readOnly = true)
    public List<GameRoomResponse> getRoomList() {

        return gameRoomRepository
                .findByStatusOrderByCreatedAtDesc(GameRoom.RoomStatus.WAITING)
                .stream()
                .map(room -> new GameRoomResponse(
                        room,
                        gamePlayerRepository.countByGameRoom(room)
                ))
                .collect(Collectors.toList());
    }

    // 방 입장
    @Transactional
    public GameRoomResponse joinRoom(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        if (gamePlayerRepository.findByGameRoomAndUser(gameRoom, user).isPresent()) {
            throw new DuplicateException("이미 입장한 방입니다.");
        }

        if (gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING) {
            throw new BadRequestException("이미 게임이 진행 중인 방입니다.");
        }

        List<GamePlayer> allPlayers = gamePlayerRepository.findByGameRoom(gameRoom);
        long playerCount = allPlayers.stream()
                .filter(p -> p.getRole() == GamePlayer.PlayerRole.PLAYER)
                .count();

        GamePlayer.PlayerRole role = playerCount >= 2
                ? GamePlayer.PlayerRole.SPECTATOR
                : GamePlayer.PlayerRole.PLAYER;

        GamePlayer gamePlayer = GamePlayer.builder()
                .gameRoom(gameRoom)
                .user(user)
                .role(role)
                .build();

        gamePlayerRepository.save(gamePlayer);

        return new GameRoomResponse(gameRoom, allPlayers.size() + 1);
    }

    // 방 퇴장
    // 게임 중 퇴장 시 → ABANDONED 처리, 방 WAITING으로 초기화
    @Transactional
    public boolean leaveRoom(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        GamePlayer gamePlayer = gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new BadRequestException("입장한 방이 아닙니다."));

        gamePlayerRepository.delete(gamePlayer);
        gamePlayerRepository.flush();

        boolean wasPlaying = gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING;

        if (wasPlaying) {
            // 진행 중인 게임만 ABANDONED 처리
            gameRepository.findByGameRoomAndStatus(gameRoom, Game.GameStatus.IN_PROGRESS)
                    .ifPresent(game -> {
                        game.abandon();
                        gameRepository.save(game);
                    });
            gameRoom.waitGame();
            gameRoomRepository.save(gameRoom);

            gamePlayerRepository.findByGameRoom(gameRoom)
                    .forEach(p -> {
                        p.cancelReady();
                        gamePlayerRepository.save(p);
                    });
        }

        // 방에 아무도 없으면 방 닫기
        if (gamePlayerRepository.countByGameRoom(gameRoom) == 0) {
            gameRoom.close();
            gameRoomRepository.save(gameRoom);
            return wasPlaying;
        }

        // 방장이 나갔으면 권한 이양
        if (gameRoom.getHost().getId().equals(user.getId())) {
            List<GamePlayer> remaining = gamePlayerRepository.findByGameRoom(gameRoom);

            User newHost = remaining.stream()
                    .filter(p -> p.getRole() == GamePlayer.PlayerRole.PLAYER)
                    .sorted(Comparator.comparing(GamePlayer::getJoinedAt))
                    .findFirst()
                    .or(() -> remaining.stream()
                            .filter(p -> p.getRole() == GamePlayer.PlayerRole.SPECTATOR)
                            .sorted(Comparator.comparing(GamePlayer::getJoinedAt))
                            .findFirst())
                    .map(GamePlayer::getUser)
                    .orElse(null);

            if (newHost != null) {
                gameRoom.updateHost(newHost);
                gameRoomRepository.save(gameRoom);
            }
        }

        return wasPlaying;
    }

    // 준비
    @Transactional
    public RoomStateResponse ready(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        GamePlayer gamePlayer = gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new BadRequestException("입장한 방이 아닙니다."));

        if (gamePlayer.getRole() == GamePlayer.PlayerRole.SPECTATOR) {
            throw new BadRequestException("관전자는 준비할 수 없습니다.");
        }

        gamePlayer.ready();
        gamePlayerRepository.save(gamePlayer);

        return buildRoomState(gameRoom);
    }

    // 준비 취소
    @Transactional
    public RoomStateResponse cancelReady(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        GamePlayer gamePlayer = gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new BadRequestException("입장한 방이 아닙니다."));

        gamePlayer.cancelReady();
        gamePlayerRepository.save(gamePlayer);

        return buildRoomState(gameRoom);
    }

    // 관전석 → 대기열
    @Transactional
    public RoomStateResponse switchToPlayer(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        if (gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING) {
            throw new BadRequestException("게임 중에는 대기열로 이동할 수 없습니다.");
        }

        List<GamePlayer> allPlayers = gamePlayerRepository.findByGameRoom(gameRoom);
        long playerCount = allPlayers.stream()
                .filter(p -> p.getRole() == GamePlayer.PlayerRole.PLAYER)
                .count();

        if (playerCount >= 2) {
            throw new BadRequestException("대기열이 가득 찼습니다.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        GamePlayer gamePlayer = gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new BadRequestException("입장한 방이 아닙니다."));

        gamePlayer.switchToPlayer();
        gamePlayerRepository.save(gamePlayer);

        return buildRoomState(gameRoom);
    }

    // 대기열 → 관전석
    @Transactional
    public RoomStateResponse switchToSpectator(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        if (gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING) {
            throw new BadRequestException("게임 중에는 관전석으로 이동할 수 없습니다.");
        }

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        GamePlayer gamePlayer = gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new BadRequestException("입장한 방이 아닙니다."));

        gamePlayer.switchToSpectator();
        gamePlayerRepository.save(gamePlayer);

        return buildRoomState(gameRoom);
    }

    // 방 상태 조회
    @Transactional(readOnly = true)
    public RoomStateResponse getRoomState(Long roomId) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        return buildRoomState(gameRoom);
    }

    // role 기반으로 플레이어/관전자 분리
    private RoomStateResponse buildRoomState(GameRoom gameRoom) {

        List<GamePlayer> allPlayers = gamePlayerRepository.findByGameRoom(gameRoom);

        List<GamePlayer> players = allPlayers.stream()
                .filter(p -> p.getRole() == GamePlayer.PlayerRole.PLAYER)
                .sorted(Comparator.comparing(GamePlayer::getJoinedAt))
                .collect(Collectors.toList());

        List<GamePlayer> spectators = allPlayers.stream()
                .filter(p -> p.getRole() == GamePlayer.PlayerRole.SPECTATOR)
                .sorted(Comparator.comparing(GamePlayer::getJoinedAt))
                .collect(Collectors.toList());

        return new RoomStateResponse(gameRoom, players, spectators);
    }

    // 게임 종료 후 다시하기
    @Transactional
    public RoomStateResponse restartGame(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new BadRequestException("입장한 방이 아닙니다."));

        List<GamePlayer> allPlayers = gamePlayerRepository.findByGameRoom(gameRoom);
        allPlayers.forEach(p -> p.cancelReady());
        gamePlayerRepository.saveAll(allPlayers);

        gameRoom.waitGame();
        gameRoomRepository.save(gameRoom);

        return buildRoomState(gameRoom);
    }

    // 방장 색상 선택 변경
    @Transactional
    public RoomStateResponse updateHostColor(Long roomId, String username, String color) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new NotFoundException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new NotFoundException("유저를 찾을 수 없습니다."));

        if (!gameRoom.getHost().getId().equals(user.getId())) {
            throw new BadRequestException("방장만 색상을 변경할 수 있습니다.");
        }

        if (gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING) {
            throw new BadRequestException("게임 중에는 색상을 변경할 수 없습니다.");
        }

        GameRoom.HostColor hostColor = GameRoom.HostColor.valueOf(color.toUpperCase());
        gameRoom.updateHostColor(hostColor);
        gameRoomRepository.save(gameRoom);

        return buildRoomState(gameRoom);
    }
}