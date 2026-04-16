package com.doomhamsters.card;

import com.doomhamsters.game.Game;
import com.doomhamsters.player.Player;

public interface CardEffect {
  public void applyEffect(Card card, Player player, Game game);
}
