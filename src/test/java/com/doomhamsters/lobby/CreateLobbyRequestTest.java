package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

/** Tests for the CreateLobbyRequest DTO. */
class CreateLobbyRequestTest {

  @Test
  void testDefaultConstructorReturnsNulls() {
    CreateLobbyRequest request = new CreateLobbyRequest();

    assertNull(request.getGroupName());
    assertNull(request.getUser());
  }

  @Test
  void testSettersAndGetters() {
    CreateLobbyRequest request = new CreateLobbyRequest();
    User user = new User("u1", "Hamster", "🐹");

    request.setGroupName("MyLobby");
    request.setUser(user);

    assertEquals("MyLobby", request.getGroupName());
    assertEquals(user, request.getUser());
  }

  @Test
  void testGroupNameCanBeUpdated() {
    CreateLobbyRequest request = new CreateLobbyRequest();
    request.setGroupName("First");
    request.setGroupName("Second");

    assertEquals("Second", request.getGroupName());
  }
}
