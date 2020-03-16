package edu.harvard.iq.dataverse.authorization.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.harvard.iq.dataverse.authorization.exceptions.AuthenticationProviderConfigurationException;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import javax.json.JsonObject;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

class AuthenticationProviderConfigurationParserTest {
    
    ObjectMapper mapper = new ObjectMapper();
    
    @ParameterizedTest
    @ValueSource(strings = {"minimal-valid.json", "title-i18n-valid.json"})
    void parseValid(String filename) throws Exception {
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/"+filename).toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        
        AuthenticationProviderConfiguration config = AuthenticationProviderConfigurationParser.parse(content);
        
        assertNotNull(config);
    }
    
    @Test
    void testUnmarshallingDeepForOptions() throws Exception {
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/options-unmarshal-test.json").toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
    
        AuthenticationProviderConfiguration config = AuthenticationProviderConfigurationParser.parse(content);
    
        List<String> expect = Arrays.asList("client_id","test","client_secret","test");
        assertThat(config.options().toString(), stringContainsInOrder(expect));
    }
    
    @Test
    void testSerialization() throws Exception {
        // given
        Path file = Paths.get(ClassLoader.getSystemResource("auth-providers/options-unmarshal-test.json").toURI());
        String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
        AuthenticationProviderConfiguration config = AuthenticationProviderConfigurationParser.parse(content);
 
        // when
        JsonObject test = AuthenticationProviderConfigurationParser.serialize(config);
        
        //then
        assertNotNull(test);
        assertEquals(mapper.writeValueAsString(config), test.toString());
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