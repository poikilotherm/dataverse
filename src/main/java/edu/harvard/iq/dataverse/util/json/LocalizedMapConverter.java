package edu.harvard.iq.dataverse.util.json;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import edu.harvard.iq.dataverse.util.BundleUtil;
import org.apache.commons.lang3.LocaleUtils;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

@Converter
public class LocalizedMapConverter implements AttributeConverter<Map<Locale,String>, String> {
    
    static ObjectMapper mapper = new ObjectMapper();
    static Logger logger = Logger.getLogger(LocalizedMapConverter.class.getCanonicalName());
    
    @Override
    public String convertToDatabaseColumn(Map<Locale,String> localeStringMap) {
        try {
            return mapper.writeValueAsString(localeStringMap);
        } catch (JsonProcessingException e) {
            logger.warning("Could not convert localized map to JSON string. " + e.getMessage());
            return null;
        }
    }
    
    @Override
    public Map<Locale,String> convertToEntityAttribute(String s) {
        try {
            JsonNode node = mapper.readTree(s);
            return parseLocalizedJSONAttributes(node);
        } catch (IOException e) {
            logger.warning("Could not convert JSON string to localized map. " + e.getMessage());
            return null;
        }
    }
    
    /**
     * De-serialize JSON for an attribute with possible i18n values.
     *
     * In case of text only, we add the value to a map using the default locale of this Dataverse instance.
     * (Getter will default to it) When the attribute is an object, we can store it in the map.
     * Validation with JSON schema is presumed to have been done before!
     */
    public static Map<Locale,String> parseLocalizedJSONAttributes(JsonNode node) {
        Map<Locale,String> map = null;
        
        if (node.isTextual()) {
            map = Collections.singletonMap(BundleUtil.getDefaultLocale(), node.textValue());
            
        } else if (node.isObject()) {
            map = new HashMap<>();
            
            // Iterate all fields of the attribute to create our map.
            // Processing the minimized object ala { "en": "...", "de": "..." } is easier and quicker
            // with good ol' iterator than using an object mapper again.
            Iterator<Map.Entry<String,JsonNode>> fields = node.fields();
            while(fields.hasNext()) {
                Map.Entry<String, JsonNode> next = fields.next();
                map.put(LocaleUtils.toLocale(next.getKey()), next.getValue().textValue());
            }
        }
        return map;
    }
}
