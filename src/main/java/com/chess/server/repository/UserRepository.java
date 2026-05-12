package com.chess.server.repository;

import com.chess.server.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {

    // 로그인 시 사용
    Optional<User> findByUsername(String username);

    // 유저 검색 시 사용
    Optional<User> findByNickname(String nickname);

    // 회원가입 시 중복 확인
    boolean existsByUsername(String username);
    boolean existsByNickname(String nickname);
    boolean existsByEmail(String email);
}