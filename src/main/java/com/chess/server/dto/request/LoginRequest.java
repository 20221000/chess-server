package com.chess.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class LoginRequest {

    @NotBlank
    private String username;    // 로그인 아이디

    @NotBlank
    private String password;    // 비밀번호
}