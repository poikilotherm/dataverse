package edu.harvard.iq.dataverse.authorization.providers;

import edu.harvard.iq.dataverse.authorization.exceptions.AuthenticationProviderConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.io.IOException;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationProviderConfigurationParserTest {
    
    @BeforeAll
    static void loadAuthSchema() throws URISyntaxException, IOException {
        /* Non working code. NPE. Can't we access resources from main in a test?
        Path file = Paths.get(ClassLoader.getSystemResource("schemas/auth-provider-schema.json").toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        System.out.println(content);
        */
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"minimal-valid.json", "title-i18n-valid.json"})
    void parseValid(String filename) throws Exception {
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/"+filename).toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        
        AuthenticationProviderConfiguration config = AuthenticationProviderConfigurationParser.parse(content);
        
        assertNotNull(config);
    }
    
    @Test
    void testValidatorLoading() throws Exception {
        assertDoesNotThrow(() -> AuthenticationProviderConfigurationParser.getValidator());
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"minimal-valid.json", "title-i18n-valid.json", "orcid-valid.json", "shib-valid.json"})
    void validateValid(String filename) throws Exception {
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/"+filename).toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        
        assertTrue(AuthenticationProviderConfigurationParser.validate(content));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {"minimal-invalid.json"})
    void validateInvalid(String filename) throws Exception {
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/"+filename).toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        
        assertThrows(AuthenticationProviderConfigurationException.class, () -> AuthenticationProviderConfigurationParser.validate(content));
    }
}