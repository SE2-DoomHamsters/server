package com.doomhamsters.gamesession.dto;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotSame;
import static org.junit.jupiter.api.Assertions.assertNull;

import org.junit.jupiter.api.Test;

class DoomDrawnEventDtoTest {

  @Test
  void returnsTypeAndDefensiveCardCopies() {
    CardDto source = card("doom-1");

    DoomDrawnEventDto event = new DoomDrawnEventDto(source);
    source.setId("mutated");

    CardDto firstRead = event.getCard();
    CardDto secondRead = event.getCard();

    assertEquals("DOOM_DRAWN", event.getType());
    assertEquals("doom-1", firstRead.getId());
    assertEquals("Doom Hamster", firstRead.getName());
    assertEquals("doom", firstRead.getType());
    assertEquals("doom-effect", firstRead.getEffectId());
    assertNotSame(firstRead, secondRead);

    firstRead.setId("changed-copy");
    assertEquals("doom-1", event.getCard().getId());
  }

  @Test
  void supportsNullCard() {
    DoomDrawnEventDto event = new DoomDrawnEventDto();

    assertNull(event.getCard());

    event.setCard(card("doom-2"));
    assertEquals("doom-2", event.getCard().getId());

    event.setCard(null);
    assertNull(event.getCard());
  }

  private CardDto card(String id) {
    CardDto card = new CardDto();
    card.setId(id);
    card.setName("Doom Hamster");
    card.setType("doom");
    card.setEffectId("doom-effect");
    return card;
  }
}
