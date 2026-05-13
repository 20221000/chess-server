package com.chess.server.dto.request;

import jakarta.validation.constraints.Email;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateEmailRequest {

    @Email(message = "이메일 형식이 올바르지 않습니다.")
    private String email;   // null 허용 (이메일 삭제 가능)
}