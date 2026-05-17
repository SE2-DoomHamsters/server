package com.doomhamsters.gamesession.cardcommands;

import com.doomhamsters.Card;
import com.doomhamsters.Game;
import com.doomhamsters.Player;
import com.doomhamsters.gamesession.GameSession;
import java.util.Collections;
import java.util.Map;
import org.apache.logging.log4j.internal.annotation.SuppressFBWarnings;

/**
 * State available while executing a card command.
 */
public class CardCommandContext {

  private final GameSession session;
  private final Game game;
  private final Player player;
  private final Card card;
  private final Map<String, Object> parameters;

  /**
   * Creates a command context.
   *
   * @param session game session
   * @param game game instance
   * @param player activating player
   * @param card activated card
   * @param parameters command parameters
   */
  @SuppressFBWarnings("EI_EXPOSE_REP2")
  public CardCommandContext(
      GameSession session,
      Game game,
      Player player,
      Card card,
      Map<String, Object> parameters) {

    this.session = session;
    this.game = game;
    this.player = player;
    this.card = new Card(card);
    this.parameters = parameters == null
        ? Collections.emptyMap()
        : Map.copyOf(parameters);
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public GameSession getSession() {
    return session;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Game getGame() {
    return game;
  }

  @SuppressFBWarnings("EI_EXPOSE_REP")
  public Player getPlayer() {
    return player;
  }

  public Card getCard() {
    return new Card(card);
  }

  public Map<String, Object> getParameters() {
    return Collections.unmodifiableMap(parameters);
  }
}
