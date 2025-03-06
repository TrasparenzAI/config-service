/*
 * Copyright (C) 2025  Consiglio Nazionale delle Ricerche
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

import java.util.ArrayList;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.stereotype.Component;

/**
 * AuthenticationProvider per l'autenticazione di tipo Basic Auth.
 *
 * @author Cristian Lucchesi
 *
 */
@Slf4j
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {

  @Value("${spring.security.user.name}")
  String username;

  @Value("${spring.security.user.password}")
  String password;

  @Override
  public Authentication authenticate(Authentication authentication) throws AuthenticationException {

    log.debug("CustomAuthenticationProvider::authenticate -> authentication = {}", authentication);
    List<GrantedAuthority> authorities = new ArrayList<GrantedAuthority>();

    String authUsername = authentication.getName();
    String authPassword = authentication.getCredentials().toString();
    
    if (!authUsername.equals(username) || !authPassword.equals(password)) {
      throw new BadCredentialsException("invalid username and password for " + username);
    }
    return new UsernamePasswordAuthenticationToken(username, password, authorities);
  }

  @Override
  public boolean supports(Class<?> authentication) {
    return authentication.equals(UsernamePasswordAuthenticationToken.class);
  }

}