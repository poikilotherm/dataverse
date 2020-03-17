package edu.harvard.iq.dataverse.authorization.providers;


import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.Optional;
import java.util.logging.Logger;

@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationProviderOptions {
    
    static ObjectMapper mapper = new ObjectMapper();
    static Logger logger = Logger.getLogger(AuthenticationProviderOptions.class.getCanonicalName());
    
    @JsonProperty("client_id")
    String oauthClientId;
    @JsonProperty("client_secret")
    String oauthClientSecret;
    @JsonProperty("user_endpoint")
    String oauthUserEndpoint;
    @JsonProperty("issuer")
    String oidcIssuerUrl;
    @JsonProperty("passive_login")
    Boolean shibPassiveLogin;
    
    @JsonIgnore
    public Optional<String> getOauthClientID() {
        return Optional.ofNullable(oauthClientId);
    }
    @JsonIgnore
    public Optional<String> getOauthClientSecret() {
        return Optional.ofNullable(oauthClientSecret);
    }
    @JsonIgnore
    public Optional<String> getOauthUserEndpoint() {
        return Optional.ofNullable(oauthUserEndpoint);
    }
    @JsonIgnore
    public Optional<String> getOidcIssuerURL() {
        return Optional.ofNullable(oidcIssuerUrl);
    }
    @JsonIgnore
    public Optional<Boolean> useShibPassiveLogin() {
        return Optional.ofNullable(shibPassiveLogin);
    }
    
    public boolean equals(Object o) {
        if (o instanceof AuthenticationProviderOptions) {
            AuthenticationProviderOptions oAPO = (AuthenticationProviderOptions)o;
            if (oAPO.getOauthClientID().equals(this.getOauthClientID()) &&
                oAPO.getOauthClientSecret().equals(this.getOauthClientSecret()) &&
                oAPO.getOauthUserEndpoint().equals(this.getOauthUserEndpoint()) &&
                oAPO.getOidcIssuerURL().equals(this.getOidcIssuerURL()) &&
                oAPO.useShibPassiveLogin().equals(this.useShibPassiveLogin())
               ) {
                return true;
            }
        }
        return false;
    }
    
    public String toString() {
        return
        "client_id: "+this.oauthClientId+", "+
        "client_secret: "+this.oauthClientSecret+", "+
        "user_endpoint: "+this.oauthUserEndpoint+", "+
        "issuer: "+this.oidcIssuerUrl+", "+
        "passive_login: "+this.shibPassiveLogin;
    }
    
    @Converter
    public static class AuthenticationProviderOptionsConverter implements AttributeConverter<AuthenticationProviderOptions, String> {
        @Override
        public String convertToDatabaseColumn(AuthenticationProviderOptions authenticationProviderOptions) {
            try {
                return mapper.writeValueAsString(authenticationProviderOptions);
            } catch (JsonProcessingException e) {
                logger.warning("Could not convert authentication provider options to JSON string. " + e.getMessage());
                return null;
            }
        }
    
        @Override
        public AuthenticationProviderOptions convertToEntityAttribute(String s) {
            try {
                return mapper.readValue(s, AuthenticationProviderOptions.class);
            } catch (IOException e) {
                logger.warning("Could not convert JSON string to authentication provider options. " + e.getMessage());
                return null;
            }
        }
    }
}
