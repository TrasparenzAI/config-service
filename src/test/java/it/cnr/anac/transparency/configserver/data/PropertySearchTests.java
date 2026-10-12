/*
 * Copyright (C) 2026 Consiglio Nazionale delle Ricerche
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU Affero General Public License as
 *     published by the Free Software Foundation, either version 3 of the
 *     License, or (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU Affero General Public License for more details.
 *
 *     You should have received a copy of the GNU Affero General Public License
 *     along with this program.  If not, see <https://www.gnu.org/licenses/>.
 */
package it.cnr.anac.transparency.configserver.data;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.is;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifica dei metodi di ricerca esposti al path /properties/search.
 *
 * @author Cristian Lucchesi
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PropertySearchTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  MockMvc mockMvc;

  @Test
  void findByKey() throws Exception {
    mockMvc.perform(get("/properties/search/findByKey").param("key", "color"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.properties", hasSize(1)))
        .andExpect(jsonPath("$._embedded.properties[0].id", is(18)))
        .andExpect(jsonPath("$._embedded.properties[0].application", is("task-scheduler-service")))
        .andExpect(jsonPath("$._embedded.properties[0].key", is("color")));
  }

  @Test
  void findByApplicationAndProfileAndKey() throws Exception {
    mockMvc.perform(get("/properties/search/findByApplicationAndProfileAndKey")
            .param("application", "task-scheduler-service")
            .param("profile", "default")
            .param("key", "workflow.cron.url")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.properties", hasSize(1)))
        .andExpect(jsonPath("$._embedded.properties[0].id", is(2)))
        .andExpect(jsonPath("$._embedded.properties[0].key", is("workflow.cron.url")));
  }

  @Test
  void findByApplicationAndProfileAndKeyWithoutResults() throws Exception {
    mockMvc.perform(get("/properties/search/findByApplicationAndProfileAndKey")
            .param("application", "rule-service")
            .param("profile", "default")
            .param("key", "color"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$._embedded.properties", hasSize(0)));
  }
}
