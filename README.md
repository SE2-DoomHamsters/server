# backend
This project is a Java-based backend server for a multiplayer card game.  
It uses WebSockets for real-time communication between clients and the server.

The system manages game state, player actions, and turn-based logic on the server side.


##Features

- Real-time multiplayer communication via WebSockets
- Server-authoritative game state management
- Modular card system (actions, effects, game rules)
- Turn-based game flow handling
- Scalable player/session handling

The backend follows a modular structure

- WebSockets are used instead of REST to support low-latency real-time gameplay.
- Game logic is server-side to prevent cheating and ensure consistency.
- Card effects are implemented using functional interfaces for flexibility.

- ## Example Usage

1. Start the server
2. Connect via WebSocket (e.g., using Postman or a frontend client)
3. Send a QR code to the host
4. Play actions using the client
5. 
