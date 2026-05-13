package com.chess.server.controller;

import com.chess.server.dto.request.UpdateEmailRequest;
import com.chess.server.dto.request.UpdateNicknameRequest;
import com.chess.server.dto.request.UpdatePasswordRequest;
import com.chess.server.dto.response.UserProfileResponse;
import com.chess.server.dto.response.UserStatsResponse;
import com.chess.server.service.UserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/users")
@RequiredArgsConstructor
public class UserController {

    private final UserService userService;

    // 내 정보 조회
    @GetMapping("/me")
    public ResponseEntity<UserProfileResponse> getMyProfile(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyProfile(userDetails.getUsername()));
    }

    // 닉네임 수정
    @PatchMapping("/me/nickname")
    public ResponseEntity<Void> updateNickname(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateNicknameRequest request) {
        userService.updateNickname(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    // 비밀번호 수정
    @PatchMapping("/me/password")
    public ResponseEntity<Void> updatePassword(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdatePasswordRequest request) {
        userService.updatePassword(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    // 이메일 수정
    @PatchMapping("/me/email")
    public ResponseEntity<Void> updateEmail(
            @AuthenticationPrincipal UserDetails userDetails,
            @Valid @RequestBody UpdateEmailRequest request) {
        userService.updateEmail(userDetails.getUsername(), request);
        return ResponseEntity.ok().build();
    }

    // 전적 조회
    @GetMapping("/me/stats")
    public ResponseEntity<UserStatsResponse> getMyStats(
            @AuthenticationPrincipal UserDetails userDetails) {
        return ResponseEntity.ok(userService.getMyStats(userDetails.getUsername()));
    }
}