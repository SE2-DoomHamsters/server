package com.doomhamsters.game;

import com.doomhamsters.card.CardType;
import com.doomhamsters.player.Deck;

public class DeckBuilder {

  public static Deck buildBaseDeck(int playerCount) {
    Deck deck = new Deck();

    deck.addCopies(CardType.SNACK_STASH, Math.max(0, 6 - playerCount));
    deck.addCopies(CardType.SIGN_OF_FATE, 4);

    deck.addCopies(CardType.POWER_NAP, 4);
    deck.addCopies(CardType.HYPER_MODE, 4);
    deck.addCopies(CardType.SQUEAK, 5);
    deck.addCopies(CardType.TUNNEL_CHAOS, 4);

    deck.addCopies(CardType.SNIFF_AHEAD, 4);
    deck.addCopies(CardType.QUICK_PEEK, 4);

    deck.addCopies(CardType.TINY_THIEF, 4);
    deck.addCopies(CardType.BEG_FOR_SNACKS, 4);
    deck.addCopies(CardType.CAGE_SWAP, 4);

    deck.addCopies(CardType.FAT_HAMSTER, 4);
    deck.addCopies(CardType.NINJA_HAMSTER, 4);
    deck.addCopies(CardType.SLEEPY_HAMSTER, 4);
    deck.addCopies(CardType.GREMLIN_HAMSTER, 4);
    deck.addCopies(CardType.ZOMBI_HAMSTER, 4);

    deck.shuffle();
    return deck;
  }

  public static void addDoomCards(Deck deck, int playerCount) {
    deck.addCopies(CardType.DOOM, playerCount - 1);
    deck.shuffle();
  }
}
