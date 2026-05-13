package com.chess.server.controller;

import com.chess.server.dto.request.UpdateRecordRequest;
import com.chess.server.dto.response.GameMoveResponse;
import com.chess.server.dto.response.GameRecordResponse;
import com.chess.server.service.GameRecordService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/records")
@RequiredArgsConstructor
public class GameRecordController {

    private final GameRecordService gameRecordService;

    // 내 기보 목록 조회
    @GetMapping("/my")
    public ResponseEntity<List<GameRecordResponse>> getMyRecords(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(gameRecordService.getMyRecords(userDetails.getUsername()));
    }

    // 저장한 기보 목록 조회
    @GetMapping("/saved")
    public ResponseEntity<List<GameRecordResponse>> getSavedRecords(
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(gameRecordService.getSavedRecords(userDetails.getUsername()));
    }

    // 기보 재생 (착수 목록 조회)
    @GetMapping("/{recordId}/moves")
    public ResponseEntity<List<GameMoveResponse>> getGameMoves(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                gameRecordService.getGameMoves(recordId, userDetails.getUsername()));
    }

    // 게임 종료 시 기보 저장
    @PostMapping("/{gameId}/save")
    public ResponseEntity<String> saveRecord(
            @PathVariable Long gameId,
            @AuthenticationPrincipal UserDetails userDetails) {

        gameRecordService.saveRecord(gameId, userDetails.getUsername());
        return ResponseEntity.ok("기보 저장 완료");
    }

    // 남의 기보 저장
    @PostMapping("/{recordId}/save-others")
    public ResponseEntity<String> saveOthersRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserDetails userDetails) {

        gameRecordService.saveOthersRecord(recordId, userDetails.getUsername());
        return ResponseEntity.ok("기보 저장 완료");
    }

    // 기보 제목 / 공개 여부 수정
    @PatchMapping("/{recordId}")
    public ResponseEntity<GameRecordResponse> updateRecord(
            @PathVariable Long recordId,
            @RequestBody UpdateRecordRequest request,
            @AuthenticationPrincipal UserDetails userDetails) {

        return ResponseEntity.ok(
                gameRecordService.updateRecord(recordId, request, userDetails.getUsername()));
    }

    // 저장한 남의 기보 삭제
    @DeleteMapping("/{recordId}/saved")
    public ResponseEntity<String> deleteSavedRecord(
            @PathVariable Long recordId,
            @AuthenticationPrincipal UserDetails userDetails) {

        gameRecordService.deleteSavedRecord(recordId, userDetails.getUsername());
        return ResponseEntity.ok("삭제 완료");
    }
}