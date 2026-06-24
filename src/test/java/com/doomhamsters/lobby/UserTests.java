package com.doomhamsters.lobby;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;

/**
 * Tests the User class for 100% code coverage.
 */
class UserTest {

  @Test
  void testGetterAndSetter() {
    User user = new User();
    user.setId("abc-123");
    user.setUsername("HamsterPro");
    user.setAvatar("🐹");

    assertEquals("abc-123", user.getId());
    assertEquals("HamsterPro", user.getUsername());
    assertEquals("🐹", user.getAvatar());
  }

  @Test
  void testConstructors() {
    // Tests the default constructor
    User emptyUser = new User();
    assertNotNull(emptyUser);

    // Tests the full-args constructor
    User fullUser = new User("id1", "name1", "icon1");
    assertEquals("id1", fullUser.getId());
    assertEquals("name1", fullUser.getUsername());
    assertEquals("icon1", fullUser.getAvatar());
  }

  @Test
  void testEqualsAndHashCode() {
    User userA = new User("100", "Michael", "🐱");
    User userB = new User("100", "Michael", "🐱");
    User userC = new User("200", "AndereID", "🐶");

    assertEquals(userA, userA);

    assertEquals(userA, userB);
    assertEquals(userA.hashCode(), userB.hashCode());

    assertNotEquals(userA, userC);
    assertNotEquals(userA, null);
    assertNotEquals(userA, new Object());
  }
}
