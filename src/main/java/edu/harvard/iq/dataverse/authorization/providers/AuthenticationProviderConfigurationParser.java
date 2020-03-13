package edu.harvard.iq.dataverse.authorization.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import edu.harvard.iq.dataverse.authorization.exceptions.AuthenticationProviderConfigurationException;

import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

public class AuthenticationProviderConfigurationParser {
    
    static ObjectMapper mapper = new ObjectMapper();
    static Logger logger = Logger.getLogger("edu.harvard.iq.dataverse.authorization.providers.AuthenticationProviderConfigurationParser");
    
    public static AuthenticationProviderConfiguration parse(String json) throws AuthenticationProviderConfigurationException {
        try {
            AuthenticationProviderConfiguration config = mapper.readValue(json, AuthenticationProviderConfiguration.class);
            return config;
        } catch (IOException e) {
            logger.warning(e.getMessage());
            throw new AuthenticationProviderConfigurationException(e.getMessage());
        }
    }
}
