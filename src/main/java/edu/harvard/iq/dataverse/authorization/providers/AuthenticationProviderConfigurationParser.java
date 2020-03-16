package edu.harvard.iq.dataverse.authorization.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ObjectReader;
import com.fasterxml.jackson.datatype.jsr353.JSR353Module;
import edu.harvard.iq.dataverse.authorization.exceptions.AuthenticationProviderConfigurationException;
import org.everit.json.schema.Schema;
import org.everit.json.schema.ValidationException;
import org.everit.json.schema.loader.SchemaLoader;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONTokener;

import javax.json.Json;
import javax.json.JsonObject;
import java.io.IOException;
import java.net.URISyntaxException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.logging.Logger;

public class AuthenticationProviderConfigurationParser {
    
    static ObjectMapper mapper = new ObjectMapper();
    static Logger logger = Logger.getLogger("AuthenticationProviderConfigurationParser");
    
    public static AuthenticationProviderConfiguration parse(String json) throws AuthenticationProviderConfigurationException {
        try {
            AuthenticationProviderConfiguration config = mapper.readValue(json, AuthenticationProviderConfiguration.class);
            return config;
        } catch (IOException e) {
            logger.warning(e.getMessage());
            throw new AuthenticationProviderConfigurationException(e.getMessage());
        }
    }
    
    public static boolean validate(String rawJson) throws AuthenticationProviderConfigurationException {
        try {
            Schema schema = getValidator();
            JSONObject json = new JSONObject(rawJson);
            schema.validate(json);
            return true;
        } catch (ValidationException | JSONException e) {
            logger.warning(e.getMessage());
            throw new AuthenticationProviderConfigurationException(e.getMessage());
        }
    }
    
    public static JsonObject serialize(AuthenticationProviderConfiguration config) {
        mapper.registerModule(new JSR353Module());
        return mapper.convertValue(config, JsonObject.class);
    }
    
    /**
     * Fetch the schema and process into usable validator based on https://github.com/everit-org/json-schema
     * @return The schema object, to be used as validator
     */
    static Schema getValidator() throws AuthenticationProviderConfigurationException {
        try {
            // retrieve schema as a resource from classpath
            Path file = Paths.get(AuthenticationProviderConfigurationParser.class.getClassLoader().getResource("schemas/auth-provider.json").toURI());
            String content = new String(Files.readAllBytes(file), StandardCharsets.UTF_8);
            // parse schema as json object
            JSONObject rawSchema = new JSONObject(new JSONTokener(content));
            // build schema for validation
            return SchemaLoader.load(rawSchema);
        } catch (IOException | URISyntaxException | JSONException e) {
            throw new AuthenticationProviderConfigurationException(e.getMessage());
        }
    }
}
