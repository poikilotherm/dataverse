package edu.harvard.iq.dataverse.authorization.exceptions;

import edu.harvard.iq.dataverse.authorization.providers.AuthenticationProvider;
import edu.harvard.iq.dataverse.authorization.providers.AuthenticationProviderFactory;
import edu.harvard.iq.dataverse.authorization.providers.AuthenticationProviderRow;

/**
 * Thrown when trying to build an {@link AuthenticationProviderConfiguration}, but failing.
 */
public class AuthenticationProviderConfigurationException extends AuthorizationSetupException {
    public AuthenticationProviderConfigurationException(String cause) {
        super(cause);
    }
}
