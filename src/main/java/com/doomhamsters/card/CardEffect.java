package com.doomhamsters.card;

import com.doomhamsters.game.Game;
import com.doomhamsters.player.Player;

@FunctionalInterface
public interface CardEffect {
  void apply(CardType card, Player player, Game game);
}
