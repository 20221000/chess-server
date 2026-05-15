package com.chess.server.dto.request;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class CreateRoomRequest {

    @NotBlank
    @Size(min = 1, max = 50)
    private String title;       // 방 제목

    @Min(2)
    @Max(10000)
    private int maxPlayers;     // 최대 인원 (2~10000명)
}