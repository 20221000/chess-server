package com.chess.server.dto.response;

import lombok.Getter;

@Getter
public class UserStatsResponse {

    private int totalGames;  // 총 게임 수
    private int wins;        // 승
    private int losses;      // 패
    private int draws;       // 무

    public UserStatsResponse(int wins, int losses, int draws) {
        this.wins = wins;
        this.losses = losses;
        this.draws = draws;
        this.totalGames = wins + losses + draws;
    }
}