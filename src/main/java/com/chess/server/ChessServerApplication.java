package com.chess.server;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

// JPA Auditing 활성화 (@CreatedDate 자동 시간 기록)
@EnableJpaAuditing
@SpringBootApplication
public class ChessServerApplication {
	public static void main(String[] args) {
		SpringApplication.run(ChessServerApplication.class, args);
	}
}