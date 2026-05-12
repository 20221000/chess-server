package com.chess.server.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class AuthResponse {

    private String token;       // JWT 토큰
    private String nickname;    // 게임 표시 닉네임
    private String username;    // 로그인 아이디
}