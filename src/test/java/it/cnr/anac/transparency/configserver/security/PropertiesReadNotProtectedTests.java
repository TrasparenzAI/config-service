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
package it.cnr.anac.transparency.configserver.security;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifica delle regole di autorizzazione con la configurazione predefinita, in cui
 * la lettura delle proprietà non è protetta.
 *
 * @author Cristian Lucchesi
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class PropertiesReadNotProtectedTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  @Autowired
  MockMvc mockMvc;

  @Test
  void getPropertiesAnonymousIsAllowed() throws Exception {
    mockMvc.perform(get("/properties")).andExpect(status().isOk());
    mockMvc.perform(get("/properties/6")).andExpect(status().isOk());
    mockMvc.perform(get("/properties/search/findByKey").param("key", "workflow.cron.url"))
        .andExpect(status().isOk());
  }

  @Test
  void patchPropertyStillRequiresAdminRole() throws Exception {
    mockMvc.perform(patch("/properties/6")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"value\": \"9\"}"))
        .andExpect(status().isUnauthorized());
    mockMvc.perform(patch("/properties/6")
            .contentType(MediaType.APPLICATION_JSON)
            .content("{\"value\": \"9\"}")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }
}
