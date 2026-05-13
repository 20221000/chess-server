package com.chess.server.dto.request;

import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class UpdateRecordRequest {

    private String title;           // 기보 제목 수정
    private String visibility;      // 공개/비공개 변경
}