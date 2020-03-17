package edu.harvard.iq.dataverse.authorization.providers;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
class AuthenticationProviderConfigurationIT {
    
    @Container
    static PostgreSQLContainer sql = new PostgreSQLContainer("postgres:9.6")
                                              .withDatabaseName("dataverse")
                                              .withUsername("dataverse")
                                              .withPassword("dataverse");
    
    EntityManager em;
    EntityTransaction tx;
    
    @BeforeEach
    public void init() {
        Map<String, String> configuration = new HashMap<>();
        configuration.put("javax.persistence.jdbc.url", sql.getJdbcUrl());
        configuration.put("javax.persistence.jdbc.user", sql.getUsername());
        configuration.put("javax.persistence.jdbc.password", sql.getPassword());
        configuration.put("javax.persistence.jdbc.driver", sql.getDriverClassName());
        this.em = Persistence.
            createEntityManagerFactory("integration-test", configuration).
            createEntityManager();
        this.tx = this.em.getTransaction();
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"minimal-valid.json", "title-i18n-valid.json", "orcid-valid.json", "options-unmarshal-test.json"})
    void testPersistence(String filename) throws Exception {
        // given
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/"+filename).toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        AuthenticationProviderConfiguration config = AuthenticationProviderConfigurationParser.parse(content);
    
        // when
        assertTrue(sql.isRunning());
        em.persist(config);
        AuthenticationProviderConfiguration readBack = em.find(AuthenticationProviderConfiguration.class, config.getId());
        
        // then
        assertTrue(config.equalsDeep(readBack));
    }
}