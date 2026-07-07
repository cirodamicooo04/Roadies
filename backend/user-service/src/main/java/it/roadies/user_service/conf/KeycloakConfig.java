package it.roadies.user_service.conf;

import org.keycloak.admin.client.Keycloak;
import org.keycloak.admin.client.KeycloakBuilder;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class KeycloakConfig {

    @Value("${keycloak.auth-server-url:http://keycloak:8080}")
    private String serverUrl;

    @Value("${keycloak.client.secret}")
    private String clientSecret;

    @Bean
    public Keycloak keycloakAdminClient() {
        return KeycloakBuilder.builder()
                .serverUrl(serverUrl)
                .realm("roadies-app")
                .grantType("client_credentials")
                .clientId("gateway-client")
                .clientSecret(clientSecret)
                .build();
    }
}