/*
 * Copyright (C) 2025 Consiglio Nazionale delle Ricerche
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

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authorization.AuthenticatedAuthorizationManager;
import org.springframework.security.authorization.AuthorizationDecision;
import org.springframework.security.authorization.AuthorizationManager;
import org.springframework.security.authorization.AuthorizationManagers;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.builders.AuthenticationManagerBuilder;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.intercept.RequestAuthorizationContext;

import javax.inject.Inject;

/**
 * Configura una doppia autenticazione, quella Basic Auth utilizzata dal client
 * Spring Cloud Config e quella OAuth2 per l'accesso alle API REST utilizzate
 * per la gestione della configurazione.
 *
 * @author Cristian Lucchesi
 */
@Slf4j
@EnableWebSecurity
@EnableMethodSecurity
@Configuration
@EnableConfigurationProperties(Oauth2Properties.class)
public class SecurityConfig {
    /**
     * Metodi di ricerca per chiave di Spring Data REST, utilizzabili senza autenticazione
     * per le chiavi indicate in security.oauth2.public-property-keys.
     */
    private static final String[] PROPERTIES_SEARCH_BY_KEY = {
            "/properties/search/findByKey",
            "/properties/search/findByApplicationAndProfileAndKey"
    };

    private final Oauth2Properties oauth2Properties;

    @Inject
    MyBasicAuthenticationEntryPoint authenticationEntryPoint;

    @Inject
    private CustomAuthenticationProvider authProvider;

    public SecurityConfig(Oauth2Properties oauth2Properties) {
        this.oauth2Properties = oauth2Properties;
    }


    @Inject
    public void configAuthentication(AuthenticationManagerBuilder auth) throws Exception {
        auth.authenticationProvider(authProvider);
    }


    /**
     * Consente l'accesso se la richiesta contiene un solo parametro key con una delle chiavi
     * pubbliche, altrimenti richiede l'autenticazione.
     */
    private AuthorizationManager<RequestAuthorizationContext> publicPropertyKeyOrAuthenticated() {
        AuthorizationManager<RequestAuthorizationContext> publicPropertyKey = (authentication, context) -> {
            String[] keys = context.getRequest().getParameterValues("key");
            return new AuthorizationDecision(keys != null && keys.length == 1
                    && oauth2Properties.getPublicPropertyKeys().contains(keys[0]));
        };
        return AuthorizationManagers.anyOf(publicPropertyKey, AuthenticatedAuthorizationManager.authenticated());
    }

    @Bean
    public SecurityFilterChain configure(HttpSecurity http) throws Exception {
        log.info("Enabling security config");
        http.httpBasic(Customizer.withDefaults());
        if (oauth2Properties.isEnabled()) {
            http.authorizeHttpRequests(expressionInterceptUrlRegistry -> {
                expressionInterceptUrlRegistry
                        .requestMatchers(HttpMethod.OPTIONS).permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health", "/actuator/health/**", "/actuator/info").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api-docs/**", "/swagger-ui/**").permitAll()
                        .requestMatchers(HttpMethod.GET, PROPERTIES_SEARCH_BY_KEY).access(publicPropertyKeyOrAuthenticated());
                oauth2Properties
                        .getRoles()
                        .forEach((key, value) ->
                                expressionInterceptUrlRegistry
                                        .requestMatchers(HttpMethod.valueOf(key)).hasAnyRole(value)
                        );
                // La configurazione Spring Cloud Config, la lettura delle proprietà e gli altri
                // endpoint dell'actuator sono accessibili solo da utenti autenticati (Basic Auth o JWT)
                expressionInterceptUrlRegistry
                        .requestMatchers("/config/**", "/actuator/**").authenticated()
                        .requestMatchers(HttpMethod.GET, "/properties", "/properties/**").authenticated()
                        .anyRequest()
                        .permitAll();
            });
        }

        return http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(config -> config.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(httpSecurityOAuth2ResourceServerConfigurer -> {
                    httpSecurityOAuth2ResourceServerConfigurer.jwt(jwtConfigurer -> {
                        jwtConfigurer.jwtAuthenticationConverter(
                                new RolesClaimConverter(new JwtGrantedAuthoritiesConverter())
                        );
                    });

                })
                .build();
    }
}