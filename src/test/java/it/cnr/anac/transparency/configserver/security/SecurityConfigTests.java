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

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

/**
 * Verifica delle regole di autorizzazione definite in {@link SecurityConfig}.
 *
 * @author Cristian Lucchesi
 */
@Testcontainers
@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTests {

  @Container
  @ServiceConnection
  static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

  private static final String CONFIG_PATH = "/config/task-scheduler-service/default";
  // Proprietà workflow.number.preserve presente nei dati iniziali
  private static final String PROPERTY_PATH = "/properties/6";

  @Autowired
  MockMvc mockMvc;

  @Value("${spring.security.user.name}")
  String username;

  @Value("${spring.security.user.password}")
  String password;

  private MockHttpServletRequestBuilder patchProperty() {
    return patch(PROPERTY_PATH)
        .contentType(MediaType.APPLICATION_JSON)
        .content("{\"value\": \"9\"}");
  }

  @Test
  void patchPropertyAnonymousIsUnauthorized() throws Exception {
    mockMvc.perform(patchProperty()).andExpect(status().isUnauthorized());
  }

  @Test
  void patchPropertyWithoutAdminRoleIsForbidden() throws Exception {
    mockMvc.perform(patchProperty()
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }

  @Test
  void patchPropertyWithAdminRoleIsAllowed() throws Exception {
    mockMvc.perform(patchProperty()
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_ADMIN"))))
        .andExpect(status().is2xxSuccessful());
  }

  @Test
  void getPropertiesAnonymousIsAllowed() throws Exception {
    mockMvc.perform(get("/properties")).andExpect(status().isOk());
  }

  @Test
  void configAnonymousIsUnauthorized() throws Exception {
    mockMvc.perform(get(CONFIG_PATH)).andExpect(status().isUnauthorized());
  }

  @Test
  void configWithWrongCredentialsIsUnauthorized() throws Exception {
    mockMvc.perform(get(CONFIG_PATH).with(httpBasic(username, "wrong-password")))
        .andExpect(status().isUnauthorized());
  }

  @Test
  void configWithBasicAuthIsAllowed() throws Exception {
    mockMvc.perform(get(CONFIG_PATH).with(httpBasic(username, password)))
        .andExpect(status().isOk());
  }

  @Test
  void actuatorHealthAndInfoAreAnonymous() throws Exception {
    mockMvc.perform(get("/actuator/health")).andExpect(status().isOk());
    mockMvc.perform(get("/actuator/info")).andExpect(status().isOk());
  }

  @Test
  void otherActuatorEndpointsRequireAuthentication() throws Exception {
    mockMvc.perform(get("/actuator/env")).andExpect(status().isUnauthorized());
    mockMvc.perform(get("/actuator")).andExpect(status().isUnauthorized());
  }

  @Test
  void actuatorRefreshRequiresAdminRole() throws Exception {
    mockMvc.perform(post("/actuator/refresh")).andExpect(status().isUnauthorized());
    mockMvc.perform(post("/actuator/refresh")
            .with(jwt().authorities(new SimpleGrantedAuthority("ROLE_USER"))))
        .andExpect(status().isForbidden());
  }
}
