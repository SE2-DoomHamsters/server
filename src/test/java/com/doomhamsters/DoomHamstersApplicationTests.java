package com.doomhamsters;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;

/** Integration tests verifying the Spring application context loads correctly. */
@SpringBootTest
@SuppressWarnings("PMD.AtLeastOneConstructor")
class DoomHamstersApplicationTests {

  @Test
  void contextLoads(@Autowired ApplicationContext context) {
    assertNotNull(context);
  }
}
