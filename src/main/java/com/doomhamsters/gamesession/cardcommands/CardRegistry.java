package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;
import com.doomhamsters.gamesession.cardcommands.cards.BegForSnacksCardDefinition;
import com.doomhamsters.gamesession.cardcommands.cards.HamsterTrioCardDefinition;
import com.doomhamsters.gamesession.cardcommands.cards.PowerNapCardDefinition;
import com.doomhamsters.gamesession.cardcommands.cards.QuickPeekCardDefinition;
import com.doomhamsters.gamesession.cardcommands.cards.SignOfFateCardDefinition;
import com.doomhamsters.gamesession.cardcommands.cards.SniffAheadCardDefinition;
import com.doomhamsters.gamesession.cardcommands.cards.StealCardDefinition;
import com.doomhamsters.gamesession.dto.CardDto;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;
import org.springframework.stereotype.Component;

/**
 * Central registry of playable card definitions.
 */
@Component
public class CardRegistry {

  private final Map<String, CardDefinition> definitionsByCommandAndType;
  private final Map<String, CardDefinition> definitionsByType;
  private final List<CardDefinition> definitions;

  /**
   * Creates the registry from discovered card definition beans.
   *
   * @param definitions card definitions to register
   */
  @SuppressFBWarnings("CT_CONSTRUCTOR_THROW")
  public CardRegistry(Collection<CardDefinition> definitions) {
    this.definitions =
        definitions.stream()
            .sorted(Comparator.comparing(CardDefinition::commandId))
            .toList();
    this.definitionsByCommandAndType = new HashMap<>();
    this.definitionsByType = new HashMap<>();

    for (CardDefinition definition : this.definitions) {
      definitionsByCommandAndType.put(
          key(definition.commandId(), definition.cardType()),
          definition);
      definitionsByCommandAndType.put(
          key(definition.commandId(), definition.commandId()),
          definition);
      definitionsByType.put(normalize(definition.cardType()), definition);
    }
  }

  /**
   * Creates a registry containing the built-in card definitions.
   *
   * @return default card registry
   */
  public static CardRegistry defaultRegistry() {
    return of(
        new PowerNapCardDefinition(),
        new QuickPeekCardDefinition(),
        new SniffAheadCardDefinition(),
        new SignOfFateCardDefinition(),
        new StealCardDefinition(),
        new BegForSnacksCardDefinition(),
        new QuickPeekCardDefinition(),
        new HamsterTrioCardDefinition(),
        new BegForSnacksCardDefinition()
    );
  }

  /**
   * Convenience factory used by direct unit tests.
   *
   * @param definitions card definitions to register
   * @return registry
   */
  public static CardRegistry of(CardDefinition... definitions) {
    return new CardRegistry(List.of(definitions));
  }

  /**
   * Looks up a card definition by client command id and card type.
   *
   * @param commandId command id
   * @param cardType card type or effect id
   * @return matching card definition
   */
  public CardDefinition get(
      String commandId,
      String cardType) {

    CardDefinition definition = definitionsByCommandAndType.get(key(commandId, cardType));
    if (definition == null) {
      throw new IllegalArgumentException(
          "No card registered for " + commandId + " and " + cardType + ".");
    }

    return definition;
  }

  /**
   * Finds a card definition by card type.
   *
   * @param cardType card type
   * @return matching card definition
   */
  public Optional<CardDefinition> findByCardType(String cardType) {
    if (cardType == null || cardType.isBlank()) {
      return Optional.empty();
    }

    return Optional.ofNullable(definitionsByType.get(normalize(cardType)));
  }

  /**
   * Creates testing starting-hand cards for a player.
   *
   * @param playerId owning player id
   * @return testing cards
   */
  public List<Card> createTestingCardsForPlayer(String playerId) {
    return definitions.stream()
        .map(definition -> definition.createTestingCard(playerId))
        .toList();
  }

  /**
   * Maps a card to a DTO, adding effectId when the card type is registered.
   *
   * @param card source card
   * @return card DTO
   */
  public CardDto toCardDto(Card card) {
    return toCardDto(
        card,
        card == null
            ? null
            : findByCardType(card.getType()).orElse(null));
  }

  /**
   * Maps a card to a DTO with the supplied definition metadata.
   *
   * @param card source card
   * @param definition card definition
   * @return card DTO
   */
  public CardDto toCardDto(
      Card card,
      CardDefinition definition) {

    if (card == null) {
      return null;
    }

    CardDto dto = new CardDto();
    dto.setId(card.getId());
    dto.setName(card.getName());
    dto.setType(card.getType());

    if (definition != null) {
      dto.setEffectId(definition.effectId());
    }

    return dto;
  }

  private String key(
      String commandId,
      String cardType) {

    return normalize(commandId) + ":" + normalize(cardType);
  }

  private String normalize(String value) {
    if (value == null || value.isBlank()) {
      throw new IllegalArgumentException("Command id and card type are required.");
    }

    return value.trim().toUpperCase(Locale.ROOT);
  }
}
