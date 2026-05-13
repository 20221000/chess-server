package com.chess.server.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateBoardRequest {

    @NotBlank(message = "제목을 입력해주세요.")
    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;           // 게시글 제목

    @NotBlank(message = "내용을 입력해주세요.")
    private String content;         // 게시글 내용

    private Long recordId;          // 게임 기보 첨부 (선택)
    private Integer startMoveNumber; // 기보 시작 수 (선택)
    private String fen;             // 임의 포지션 FEN (선택)
}