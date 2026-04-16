package com.doomhamsters.controller;

import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/** Ping Pong Test*/
@Controller
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class HelloController {

  /**
   * Receives a text message on {/app/hello} and broadcasts the reply to
   * every subscriber of {/topic/test}.
   *
   * @param message the raw text sent by the client
   * @return a plain confirmation string broadcast to {@code /topic/test}
   */
  @MessageMapping("/hello")
  @SendTo("/topic/test")
  public String handleHello(String message) {
    return "[BACKEND]: Hello App!" + "\nI received from you the following text: [APP]: " + message;
  }
}
