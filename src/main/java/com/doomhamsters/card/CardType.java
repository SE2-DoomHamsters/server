package com.doomhamsters.card;

import com.doomhamsters.game.Game;
import com.doomhamsters.player.Player;

public enum CardType {
  //Fields
  DOOM("Doom", (card, player, game) -> {}),
  SNACK_STASH("Snack Stash", (card, player, game) -> {}),
  SIGN_OF_FATE("Sign of Fate", (card, player, game) -> {}),
  POWER_NAP("Power Nap", (card, player, game) -> {}),
  HYPER_MODE("Hyper Mode", (card, player, game) -> {}),
  SQUEAK("Squeak!", (card, player, game) -> {}),
  TUNNEL_CHAOS("Tunnel Chaos", (card, player, game) -> {}),
  SNIFF_AHEAD("Sniff Ahead", (card, player, game) -> {}),
  QUICK_PEEK("Quick Peek", (card, player, game) -> {}),
  TINY_THIEF("Tiny Thief", (card, player, game) -> {}),
  BEG_FOR_SNACKS("Beg for Snacks", (card, player, game) -> {}),
  CAGE_SWAP("Cage Swap", (card, player, game) -> {}),
  FAT_HAMSTER("Fat Hamster", (card, player, game) -> {}),
  NINJA_HAMSTER("Ninja Hamster", (card, player, game) -> {}),
  SLEEPY_HAMSTER("Sleepy Hamster", (card, player, game) -> {}),
  GREMLIN_HAMSTER("Gremlin Hamster", (card, player, game) -> {}),
  ZOMBI_HAMSTER("Zombi Hamster", (card, player, game) -> {});

  private final String name;
  private final CardEffect effect;

  //Constructors
  CardType(String name, CardEffect effect) {
    this.name = name;
    this.effect = effect;
  }

  //Logic
  public void apply(Player player, Game game) {
    effect.apply(this, player, game);
  }

  //Getters & Setters
  public String getName() {
    return name;
  }
}
