package edu.harvard.iq.dataverse.authorization.providers;

import com.fasterxml.jackson.annotation.*;
import com.fasterxml.jackson.databind.JsonNode;
import edu.harvard.iq.dataverse.util.BundleUtil;
import edu.harvard.iq.dataverse.util.json.LocalizedMapConverter;
import org.apache.commons.lang3.LocaleUtils;

import java.util.*;
import javax.persistence.*;
import javax.validation.constraints.NotNull;

/**
 * Database-storable form of an {@code AuthenticationProvider} configuration.
 * An instance of this class being a configuration is translated to an actual {@link AuthenticationProvider}
 * instance by the {@link AuthenticationProviderFactory}.
 */
@NamedQueries({
    @NamedQuery( name="AuthenticationProviderConfiguration.findAllEnabled",
        query="SELECT c FROM AuthenticationProviderConfiguration c WHERE c.enabled=true" ),
    @NamedQuery( name="AuthenticationProviderConfiguration.findById",
        query="SELECT c FROM AuthenticationProviderConfiguration c WHERE c.id=:id" ),
    @NamedQuery( name="AuthenticationProviderConfiguration.findAll",
        query="SELECT c FROM AuthenticationProviderConfiguration c" )
})
@Entity
@Table(indexes = {@Index(columnList="enabled")})
@JsonInclude(JsonInclude.Include.NON_NULL)
public class AuthenticationProviderConfiguration implements java.io.Serializable {
    
    @Id
    @JsonProperty("id")
    @Column(length = 120)
    private String id;
    
    @NotNull
    @Column(length = 20)
    private String type;
    
    @NotNull
    @JsonProperty("enabled")
    private Boolean enabled;
    
    @Lob
    @JsonProperty("logo")
    private String logo;
    
    @Lob
    @NotNull
    @Convert(converter = LocalizedMapConverter.class)
    @JsonProperty("title")
    private Map<Locale,String> title;
    
    @Lob
    @Convert(converter = LocalizedMapConverter.class)
    @JsonProperty("description")
    private Map<Locale,String> description;
    
    @Lob
    @JsonProperty("options")
    @Convert(converter = AuthenticationProviderOptions.AuthenticationProviderOptionsConverter.class)
    private AuthenticationProviderOptions options;
    
    public String getId() {
        return id;
    }
    public void setId(String id) {
        this.id = id;
    }
    
    public String getType() {
        return type;
    }
    
    public boolean isEnabled() {
        return enabled;
    }
    
    public String getLogo() {
        return logo;
    }
    public void setLogo(String logo) {
        this.logo = logo;
    }
    
    /**
     * De-serialize JSON for title attribute.
     */
    @JsonSetter("title")
    public void parseTitle(JsonNode node) {
        this.title = LocalizedMapConverter.parseLocalizedJSONAttributes(node);
    }
    
    /**
     * Receive title string in i18n form
     * @param locale The locale to lookup the title.
     * @return An optional string, being empty if not found and no default.
     */
    @JsonIgnore
    public Optional<String> getTitle(Locale locale) {
        return Optional.ofNullable(title.getOrDefault(locale, title.get(BundleUtil.getDefaultLocale())));
    }
    
    /**
     * Receive title string in i18n form
     * @param locale The locale to lookup the title as string tag, following IETF BCP 47, e.g. 'en' or 'en-US'.
     * @return An optional string, being empty if not found and no default.
     */
    @JsonIgnore
    public Optional<String> getTitle(String locale) {
        Locale lookup = LocaleUtils.toLocale(locale);
        return getTitle(lookup);
    }
    
    /**
     * Receive title string using system default locale
     * @return An optional string, being empty if not found and no default.
     */
    @JsonIgnore
    public Optional<String> getTitle() {
        return getTitle(BundleUtil.getDefaultLocale());
    }
    
    /**
     * De-serialize JSON for description attribute.
     */
    @JsonSetter("description")
    public void parseDescription(JsonNode node) {
        this.description = LocalizedMapConverter.parseLocalizedJSONAttributes(node);
    }
    
    /**
     * Receive description string in i18n form
     * @param locale The locale to lookup the description.
     * @return An optional string, being empty if not found and no default.
     */
    @JsonIgnore
    public Optional<String> getDescription(Locale locale) {
        return Optional.ofNullable(description.getOrDefault(locale, description.get(BundleUtil.getDefaultLocale())));
    }
    
    /**
     * Receive description string in i18n form
     * @param locale The locale to lookup the description as string tag, following IETF BCP 47, e.g. 'en' or 'en-US'.
     * @return An optional string, being empty if not found and no default.
     */
    @JsonIgnore
    public Optional<String> getDescription(String locale) {
        Locale lookup = LocaleUtils.toLocale(locale);
        return getDescription(lookup);
    }
    
    /**
     * Receive description string using system default locale
     * @return An optional string, being empty if not found and no default.
     */
    @JsonIgnore
    public Optional<String> getDescription() {
        return getDescription(BundleUtil.getDefaultLocale());
    }
    
    /**
     * Retrieve the options configured for this provider
     * @return Provider options
     */
    public AuthenticationProviderOptions options() {
        return this.options;
    }
    
    @Override
    public int hashCode() {
        int hash = 42;
        hash = 55 * hash + Objects.hashCode(this.id);
        return hash;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if ( !(obj instanceof AuthenticationProviderConfiguration)) {
            return false;
        }
        final AuthenticationProviderConfiguration other = (AuthenticationProviderConfiguration) obj;
        return Objects.equals(this.id, other.id);
    }
    
}
