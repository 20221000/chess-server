package com.chess.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateNicknameRequest {

    @NotBlank(message = "닉네임을 입력해주세요.")
    @Size(min = 2, max = 50, message = "닉네임은 2~50자 사이여야 합니다.")
    private String nickname;
}