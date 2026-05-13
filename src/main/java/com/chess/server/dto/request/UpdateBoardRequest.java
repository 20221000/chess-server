package com.chess.server.dto.request;

import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateBoardRequest {

    @Size(max = 100, message = "제목은 100자 이하여야 합니다.")
    private String title;           // 수정할 제목 (null이면 유지)

    private String content;         // 수정할 내용 (null이면 유지)
    private Long recordId;          // 수정할 기보 (null이면 유지)
    private Integer startMoveNumber; // 수정할 기보 시작 수
    private String fen;             // 수정할 FEN (null이면 유지)
    private boolean removeAttachment; // 첨부 제거 여부
}