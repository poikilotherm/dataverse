package edu.harvard.iq.dataverse.authorization.providers;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class AuthenticationProviderOptionsTest {
    
    static ObjectMapper mapper = new ObjectMapper();
    
    @Test
    void testUnmarshaling() throws Exception {
        // given
        String json = "{\"client_id\": \"test\", \"client_secret\": \"test\", \"user_endpoint\": \"test\", \"issuer\": \"test\", \"passive_login\": true }";
        AuthenticationProviderOptions expect = new AuthenticationProviderOptions();
        expect.oauthClientId = "test";
        expect.oauthClientSecret = "test";
        expect.oauthUserEndpoint = "test";
        expect.oidcIssuerUrl = "test";
        expect.shibPassiveLogin = true;
        
        // when
        AuthenticationProviderOptions opts = mapper.readValue(json, AuthenticationProviderOptions.class);
        // then
        assertEquals(expect, opts);
    }
    
    @Test
    void testUnmarshalingPartial() throws Exception {
        // given
        String json = "{\"client_id\": \"test\", \"client_secret\": \"test\" }";
        AuthenticationProviderOptions expect = new AuthenticationProviderOptions();
        expect.oauthClientId = "test";
        expect.oauthClientSecret = "test";
        
        // when
        AuthenticationProviderOptions opts = mapper.readValue(json, AuthenticationProviderOptions.class);
        // then
        assertEquals(expect, opts);
    }
    
    @Test
    void testMarshalingComplete() throws Exception {
        // given
        String expect = "{\"client_id\":\"test\",\"client_secret\":\"test\",\"user_endpoint\":\"test\",\"issuer\":\"test\",\"passive_login\":true}";
        AuthenticationProviderOptions opts = new AuthenticationProviderOptions();
        opts.oauthClientId = "test";
        opts.oauthClientSecret = "test";
        opts.oauthUserEndpoint = "test";
        opts.oidcIssuerUrl = "test";
        opts.shibPassiveLogin = true;
        
        // when
        String json = mapper.writeValueAsString(opts);
        // then
        assertEquals(expect, json);
    }
    
    @Test
    void testMarshalingPartial() throws Exception {
        // given
        String expect = "{\"client_id\":\"test\",\"client_secret\":\"test\",\"issuer\":\"test\"}";
        AuthenticationProviderOptions opts = new AuthenticationProviderOptions();
        opts.oauthClientId = "test";
        opts.oauthClientSecret = "test";
        opts.oidcIssuerUrl = "test";
        
        // when
        String json = mapper.writeValueAsString(opts);
        // then
        assertEquals(expect, json);
    }
    
    @Test
    void testConverter() {
        // given
        AuthenticationProviderOptions expect = new AuthenticationProviderOptions();
        expect.oauthClientId = "test";
        expect.oauthClientSecret = "test";
        expect.oidcIssuerUrl = "test";
        AuthenticationProviderOptions.AuthenticationProviderOptionsConverter conv = new AuthenticationProviderOptions.AuthenticationProviderOptionsConverter();
        
        // when
        String json = conv.convertToDatabaseColumn(expect);
        AuthenticationProviderOptions opts = conv.convertToEntityAttribute(json);
        
        // then
        assertEquals(expect, opts);
    }
    
    @Test
    void testConverterUnmarshalingInvalid() throws Exception {
        // given
        String invalidJson = "{\"client_id\": \"test\", client_secret: \"test\", }";
        AuthenticationProviderOptions.AuthenticationProviderOptionsConverter conv = new AuthenticationProviderOptions.AuthenticationProviderOptionsConverter();
        // when
        AuthenticationProviderOptions opts = conv.convertToEntityAttribute(invalidJson);
        // then
        assertNull(opts);
    }
    
}