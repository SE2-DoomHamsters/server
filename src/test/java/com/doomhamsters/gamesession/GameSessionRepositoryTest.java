package com.doomhamsters.gamesession;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class GameSessionRepositoryTest {

  private GameSessionRepository repository;

  @BeforeEach
  void setUp() {
    repository = new GameSessionRepository();
  }

  @Test
  void testStoreAndFindById() {
    GameSession session = new GameSession("game-1", "lobby-1");
    repository.store(session);

    Optional<GameSession> retrieved = repository.findById("game-1");
    assertTrue(retrieved.isPresent());
    assertEquals("lobby-1", retrieved.get().getLobbyId());
  }

  @Test
  void testFindByIdNotFound() {
    Optional<GameSession> retrieved = repository.findById("non-existent");
    assertFalse(retrieved.isPresent());
  }

  @Test
  void testGetAllReturnsSnapshot() {
    repository.store(new GameSession("game-1", "lobby-1"));

    ConcurrentHashMap<String, GameSession> allSessions = repository.getAll();
    assertEquals(1, allSessions.size());

    allSessions.remove("game-1");
    assertTrue(repository.findById("game-1").isPresent());
  }

  @Test
  void testLoadAll() {
    ConcurrentHashMap<String, GameSession> initialData = new ConcurrentHashMap<>();
    initialData.put("game-1", new GameSession("game-1", "lobby-1"));

    repository.loadAll(initialData);

    assertTrue(repository.findById("game-1").isPresent());
  }
}
