package com.chess.server.service;

import com.chess.server.dto.request.CreateRoomRequest;
import com.chess.server.dto.response.GameRoomResponse;
import com.chess.server.entity.GamePlayer;
import com.chess.server.entity.GameRoom;
import com.chess.server.entity.User;
import com.chess.server.repository.GamePlayerRepository;
import com.chess.server.repository.GameRoomRepository;
import com.chess.server.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class GameRoomService {

    private final GameRoomRepository gameRoomRepository;
    private final GamePlayerRepository gamePlayerRepository;
    private final UserRepository userRepository;

    // 방 생성
    @Transactional
    public GameRoomResponse createRoom(CreateRoomRequest request, String username) {

        User host = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        GameRoom gameRoom = GameRoom.builder()
                .title(request.getTitle())
                .maxPlayers(request.getMaxPlayers())
                .host(host)
                .build();

        gameRoomRepository.save(gameRoom);

        // 방장을 자동으로 대기열에 추가
        GamePlayer gamePlayer = GamePlayer.builder()
                .gameRoom(gameRoom)
                .user(host)
                .build();

        gamePlayerRepository.save(gamePlayer);

        return new GameRoomResponse(gameRoom, 1);
    }

    // 대기중인 방 목록 조회 (로비)
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
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        // 이미 입장한 유저인지 확인
        if (gamePlayerRepository.findByGameRoomAndUser(gameRoom, user).isPresent()) {
            throw new IllegalArgumentException("이미 입장한 방입니다.");
        }

        // 최대 인원 확인
        int currentPlayers = gamePlayerRepository.countByGameRoom(gameRoom);
        if (currentPlayers >= gameRoom.getMaxPlayers()) {
            throw new IllegalArgumentException("방이 가득 찼습니다.");
        }

        // 게임 중인 방 입장 불가
        if (gameRoom.getStatus() == GameRoom.RoomStatus.PLAYING) {
            throw new IllegalArgumentException("이미 게임이 진행 중인 방입니다.");
        }

        GamePlayer gamePlayer = GamePlayer.builder()
                .gameRoom(gameRoom)
                .user(user)
                .build();

        gamePlayerRepository.save(gamePlayer);

        return new GameRoomResponse(gameRoom, currentPlayers + 1);
    }

    // 방 퇴장
    @Transactional
    public void leaveRoom(Long roomId, String username) {

        GameRoom gameRoom = gameRoomRepository.findById(roomId)
                .orElseThrow(() -> new IllegalArgumentException("방을 찾을 수 없습니다."));

        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new IllegalArgumentException("유저를 찾을 수 없습니다."));

        GamePlayer gamePlayer = gamePlayerRepository.findByGameRoomAndUser(gameRoom, user)
                .orElseThrow(() -> new IllegalArgumentException("입장한 방이 아닙니다."));

        gamePlayerRepository.delete(gamePlayer);

        // 방에 아무도 없으면 방 닫기
        if (gamePlayerRepository.countByGameRoom(gameRoom) == 0) {
            gameRoom.close();
            gameRoomRepository.save(gameRoom);
        }
    }
}