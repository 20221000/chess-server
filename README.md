# Chess Server

Online chess platform backend server.

## Related Repository
- Frontend: [chess-client](https://github.com/20221000/chess-client)

## Tech Stack
- Java 21
- Spring Boot 4.0.6
- Spring Security (JWT)
- Spring WebSocket (STOMP)
- Spring Data JPA (Hibernate)
- MySQL 9.5

## Features
- Sign up / Login (JWT Authentication)
- Real-time chess game (WebSocket STOMP)
- Game room creation / joining / queue / spectator
- Piece color selection and host authority transfer
- Game record save / view / public-private setting
- Solo game (vs self)
- Board CRUD / Comments
- User search and match history
