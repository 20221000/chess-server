package com.chess.server.entity;

import jakarta.persistence.*;
import lombok.*;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@Entity
@Table(name = "users")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@EntityListeners(AuditingEntityListener.class)
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true, length = 50)
    private String username;        // 로그인 아이디

    @Column(nullable = false)
    private String password;        // 암호화된 비밀번호

    @Column(nullable = false, unique = true, length = 50)
    private String nickname;        // 게임 표시 닉네임

    @Column(unique = true, length = 100)
    private String email;           // 이메일 (선택)

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private UserStatus status = UserStatus.ACTIVE;  // 계정 상태

    @CreatedDate
    @Column(nullable = false, updatable = false)
    private LocalDateTime createdAt;    // 가입일

    // 계정 상태 (ACTIVE: 정상, INACTIVE: 비활성)
    public enum UserStatus {
        ACTIVE, INACTIVE
    }

    // 닉네임 수정
    public void updateNickname(String nickname) {
        this.nickname = nickname;
    }

    // 비밀번호 수정 (암호화된 값을 받음)
    public void updatePassword(String encodedPassword) {
        this.password = encodedPassword;
    }

    // 이메일 수정
    public void updateEmail(String email) {
        this.email = email;
    }

    // 회원 탈퇴 (계정 비활성화)
    public void withdraw() {
        this.status = UserStatus.INACTIVE;
    }
}