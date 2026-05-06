public final class Player {
  /** Holds mutable state for a single player within a game session. */


    private final String id;
    private int lives;
    private boolean eliminated;

    public Player(String id, int lives) {
      this.id = id;
      this.lives = lives;
      this.eliminated = false;
    }

    public String getId() { return id; }
    public int getLives() { return lives; }
    public boolean isEliminated() { return eliminated; }

    /** Decrements lives by one and marks the player eliminated if lives reach zero. */
    public void decrementLives() {
      if (eliminated) return;
      lives = Math.max(0, lives - 1);
      if (lives == 0) {
        eliminated = true;
      }
    }
  }

