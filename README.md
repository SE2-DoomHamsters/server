# Backend
This project is a Java-based backend server for a multiplayer card game.  
It uses WebSockets for real-time communication between clients and the server.

The system manages game state, player actions, and turn-based logic on the server side.


## API Documentation

| What | Where |
|------|-------|
| REST API (Swagger UI) — local | `http://localhost:8080/swagger-ui.html` |
| REST API (Swagger UI) — university server | `http://se2-demo.aau.at:53217/swagger-ui.html` |
| WebSocket/STOMP channels (AsyncAPI) | [`asyncapi.yml`](asyncapi.yml) in the project root |
| Java source documentation (Javadoc) | Generate with `./mvnw javadoc:javadoc`, then open `target/reports/apidocs/index.html` |

**Swagger UI** — start the server (`./mvnw spring-boot:run`) and open the URL above in a browser. No extra steps needed.

**AsyncAPI** — open [`asyncapi.yml`](asyncapi.yml) directly, or paste it into [studio.asyncapi.com](https://studio.asyncapi.com) for a rendered view.

**Javadoc** — run `./mvnw javadoc:javadoc` and open `target/reports/apidocs/index.html` in a browser.

## Features

- Real-time multiplayer communication via WebSockets
- Server-authoritative game state management
- Modular card system (actions, effects, game rules)
- Turn-based game flow handling
- Scalable player/session handling

The backend follows a modular structure

- WebSockets are used instead of REST to support low-latency real-time gameplay.
- Game logic is server-side to prevent cheating and ensure consistency.
- CardType effects are implemented using functional interfaces for flexibility.

## Example Usage

1. Start the server
2. Connect via WebSocket (e.g., using Postman or a frontend client)
3. Send a QR code to the host
4. Play actions using the client
