package com.doomhamsters;

/**
 * A frozen copy of the game state taken just before a card command was executed.
 *
 * <p>Stored transiently on {@link Board}; never serialized or persisted.
 * After a server restart there is no prior action to undo.
 */
public class LastActionSnapshot {

  private final Game game;
  private final String commandId;

  /**
   * Creates a snapshot.
   *
   * @param game      state of the game before the command ran; stored as a deep copy
   * @param commandId command id of the action being recorded
   */
  public LastActionSnapshot(Game game, String commandId) {
    this.game = new Game(game);
    this.commandId = commandId;
  }

  /**
   * Returns a defensive copy of the recorded game state.
   *
   * @return game state before the command ran
   */
  public Game getGame() {
    return new Game(game);
  }

  /**
   * Returns the command id of the action that was recorded.
   *
   * @return command id
   */
  public String getCommandId() {
    return commandId;
  }
}
