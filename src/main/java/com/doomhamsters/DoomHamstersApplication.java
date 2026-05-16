package com.doomhamsters;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * Main entry point for the Doom Hamsters server.
 */
@SpringBootApplication
@SuppressWarnings({"PMD.UseUtilityClass", "PMD.AtLeastOneConstructor"})
public class DoomHamstersApplication {

  /**
   * Starts the Spring Boot application.
   */
  public static void main(String[] args) {
    SpringApplication.run(DoomHamstersApplication.class, args);
  }
}
