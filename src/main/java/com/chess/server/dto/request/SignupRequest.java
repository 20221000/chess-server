package com.chess.server.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class SignupRequest {

    @NotBlank
    @Size(min = 4, max = 50)
    private String username;    // 로그인 아이디

    @NotBlank
    @Size(min = 4, max = 20)
    private String password;    // 비밀번호

    @NotBlank
    @Size(min = 2, max = 50)
    private String nickname;    // 게임 표시 닉네임

    @Email
    private String email;       // 이메일 (선택)
}