package com.doomhamsters.controller;

import static org.junit.jupiter.api.Assertions.assertEquals;

import org.junit.jupiter.api.Test;

class HelloControllerTest {

  @Test
  void handleHelloReturnsBackendEcho() {
    HelloController controller = new HelloController();

    String response = controller.handleHello("ping");

    assertEquals(
        "[BACKEND]: Hello App!\nI received from you the following text: [APP]: ping",
        response);
  }
}
