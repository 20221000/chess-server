package com.chess.server.controller;

import com.chess.server.dto.response.GameRecordResponse;
import com.chess.server.dto.response.UserSearchResponse;
import com.chess.server.service.UserSearchService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserSearchController {

    private final UserSearchService userSearchService;

    // 닉네임으로 유저 검색
    @GetMapping("/search")
    public ResponseEntity<UserSearchResponse> searchUser(
            @RequestParam String nickname) {

        return ResponseEntity.ok(userSearchService.searchUser(nickname));
    }

    // 특정 유저의 공개 기보 목록 조회
    @GetMapping("/{userId}/records")
    public ResponseEntity<List<GameRecordResponse>> getUserPublicRecords(
            @PathVariable Long userId) {

        return ResponseEntity.ok(userSearchService.getUserPublicRecords(userId));
    }
}