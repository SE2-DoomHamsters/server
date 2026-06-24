package com.doomhamsters.gamesession.cardcommands;

/**
 * Base implementation for card definitions that supplies the three common identity methods.
 *
 * <p>Subclasses pass the identity strings once to the constructor and only need to implement
 * {@link CardCommand#execute(CardCommandContext)}.
 */
public abstract class AbstractCardDefinition implements CardDefinition {

  private final String cardType;
  private final String commandId;
  private final String displayName;

  protected AbstractCardDefinition(String cardType, String commandId, String displayName) {
    this.cardType = cardType;
    this.commandId = commandId;
    this.displayName = displayName;
  }

  @Override
  public String cardType() {
    return cardType;
  }

  @Override
  public String commandId() {
    return commandId;
  }

  @Override
  public String displayName() {
    return displayName;
  }
}
