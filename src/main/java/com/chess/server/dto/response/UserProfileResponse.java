package com.chess.server.dto.response;

import com.chess.server.entity.User;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
public class UserProfileResponse {

    private Long id;
    private String username;
    private String nickname;
    private String email;
    private String status;
    private LocalDateTime createdAt;

    // Entity → DTO 변환
    public UserProfileResponse(User user) {
        this.id = user.getId();
        this.username = user.getUsername();
        this.nickname = user.getNickname();
        this.email = user.getEmail();
        this.status = user.getStatus().name();
        this.createdAt = user.getCreatedAt();
    }
}