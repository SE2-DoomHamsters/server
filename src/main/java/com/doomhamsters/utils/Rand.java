package com.doomhamsters.utils;

import java.util.concurrent.ThreadLocalRandom;

public final class Rand {

  private Rand() {}

  // int [min, max] inclusive
  public static int range(int min, int max) {
    return ThreadLocalRandom.current().nextInt(min, max + 1);
  }

  // float [min, max)
  public static float range(float min, float max) {
    return ThreadLocalRandom.current().nextFloat() * (max - min) + min;
  }

  // double [min, max)
  public static double range(double min, double max) {
    return ThreadLocalRandom.current().nextDouble(min, max);
  }

  // 0.0–1.0
  public static float value() {
    return ThreadLocalRandom.current().nextFloat();
  }

  // true/false 50%
  public static boolean bool() {
    return ThreadLocalRandom.current().nextBoolean();
  }

  // probability: chance(0.3) = 30%
  public static boolean chance(double probability) {
    return ThreadLocalRandom.current().nextDouble() < probability;
  }

  // pick random element
  public static <T> T pick(T[] array) {
    return array[range(0, array.length - 1)];
  }

  // pick random from list
  public static <T> T pick(java.util.List<T> list) {
    return list.get(range(0, list.size() - 1));
  }
}
