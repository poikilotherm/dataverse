package edu.harvard.iq.dataverse.util.json;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.JsonNode;
import edu.harvard.iq.dataverse.util.BundleUtil;
import org.apache.commons.lang3.LocaleUtils;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

import java.io.IOException;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;
import static java.util.Arrays.asList;

class LocalizedMapConverterTest {
    
    LocalizedMapConverter converter = new LocalizedMapConverter();
    
    private static Stream<Arguments> validJsonExamples() {
        Map<Locale,String> sizeOne = new HashMap<>();
        Map<Locale,String> sizeTwo = new HashMap<>();
        
        sizeOne.put(LocaleUtils.toLocale("en"), "Test");
        sizeTwo.put(LocaleUtils.toLocale("en"), "Test");
        sizeTwo.put(LocaleUtils.toLocale("en_US"), "Test_US");
        
        return Stream.of(
            Arguments.of(sizeOne),
            Arguments.of(sizeTwo)
        );
    }
    private static Stream<Arguments> invalidJsonExamples() {
        return Stream.of(Arguments.of(null, "Test"));
    }
    
    @DisplayName("To DB - valid maps")
    @ParameterizedTest
    @MethodSource("validJsonExamples")
    void convertToDB_valid(Map<Locale,String> map) {
        // when
        String json = converter.convertToDatabaseColumn(map);
        
        // then
        assertNotNull(json);
        map.forEach((lang, value) -> {assertThat(json, stringContainsInOrder(asList(lang.toString(), value)));});
    }
    
    @DisplayName("To DB - invalid maps")
    @ParameterizedTest
    @MethodSource("invalidJsonExamples")
    void convertToDB_invalid(Locale lang, String value) {
        // given
        Map<Locale,String> map = new HashMap<>();
        map.put(lang, value);
        
        // when
        String json = converter.convertToDatabaseColumn(map);
        
        // then
        assertNull(json);
    }
    
    @DisplayName("To Object - verify chain object -> database -> object")
    @ParameterizedTest
    @MethodSource("validJsonExamples")
    void convertToObj_valid(Map<Locale,String> map) {
        // given
        String json = converter.convertToDatabaseColumn(map);
        // when
        Map<Locale,String> conv = converter.convertToEntityAttribute(json);
        // then
        assertNotNull(conv);
        assertEquals(map, conv);
    }
    
    private static Stream<Arguments> validParserExamples() {
        return Stream.of(
            Arguments.of("\"Test\"", BundleUtil.getDefaultLocale(), "Test"),
            Arguments.of("{\"en\": \"Test\"}", new Locale("en"), "Test"),
            Arguments.of("{\"en_US\": \"Test_US\"}", new Locale("en","US"), "Test_US")
        );
    }
    @ParameterizedTest
    @DisplayName("Parse JSON node to localized map")
    @MethodSource("validParserExamples")
    void parseLocalizedJSONAttributes(String rawjson, Locale lang, String value) throws IOException {
        // given
        JsonNode node = new ObjectMapper().readTree(rawjson);
        // when
        Map<Locale,String> map = LocalizedMapConverter.parseLocalizedJSONAttributes(node);
        // then
        assertNotNull(map);
        assertEquals(value, map.get(lang));
        assertTrue(map.containsKey(lang));
    }
}