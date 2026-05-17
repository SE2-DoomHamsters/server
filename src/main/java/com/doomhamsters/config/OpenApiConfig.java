package com.doomhamsters.config;

import  io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.servers.Server;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Configures the OpenAPI (Swagger) documentation metadata.
 *
 * <p>The generated UI is available at /swagger-ui.html when the server is running.
 */
@Configuration
@SuppressWarnings("PMD.AtLeastOneConstructor")
public class OpenApiConfig {

  /**
   * Defines the OpenAPI metadata shown in Swagger UI.
   *
   * @return configured OpenAPI instance
   */
  @Bean
  public OpenAPI customOpenApi() {
    return new OpenAPI()
        .info(new Info()
            .title("DoomHamsters Game API")
            .description("REST and WebSocket API for the DoomHamsters card game server. "
                + "For WebSocket/STOMP channel documentation see asyncapi.yml.")
            .version("1.0.0")
            .contact(new Contact()
                .name("Gruppe Beta — Doom Hamsters")
                .url("https://github.com/SE2-DoomHamsters/server")))
        .servers(List.of(
            new Server()
                .url("http://localhost:8080")
                .description("Local development"),
            new Server()
                .url("http://se2-demo.aau.at:53217")
                .description("University server (doco-cd deployment)")))
        .externalDocs(new ExternalDocumentation()
            .description("WebSocket API documentation (AsyncAPI)")
            .url("https://github.com/SE2-DoomHamsters/server/blob/main/asyncapi.yml"));
  }
}
