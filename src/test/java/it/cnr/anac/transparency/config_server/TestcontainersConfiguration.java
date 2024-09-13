package it.cnr.anac.transparency.config_server;

import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Bean;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.utility.DockerImageName;

@TestConfiguration(proxyBeanMethods = false)
class TestcontainersConfiguration {

  @Bean
  @ServiceConnection(name = "postgres")
  GenericContainer<?> postgresContainer() {
    try (GenericContainer<?> genericContainer = new GenericContainer<>(DockerImageName.parse("postgres:latest"))) {
      return genericContainer.withExposedPorts(5432);
    }
  }

}
