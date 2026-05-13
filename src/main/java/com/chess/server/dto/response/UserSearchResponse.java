package com.chess.server.dto.response;

import com.chess.server.entity.User;
import lombok.Getter;

@Getter
public class UserSearchResponse {

    private Long id;
    private String nickname;        // 닉네임
    private String username;        // 아이디

    public UserSearchResponse(User user) {
        this.id = user.getId();
        this.nickname = user.getNickname();
        this.username = user.getUsername();
    }
}