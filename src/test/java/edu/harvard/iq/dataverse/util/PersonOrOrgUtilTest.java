package edu.harvard.iq.dataverse.util;

import edu.harvard.iq.dataverse.util.json.JsonUtil;

import edu.harvard.iq.dataverse.util.testing.SystemProperty;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import jakarta.json.JsonObject;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.NullAndEmptySource;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

@SystemProperty(key = "dataverse.personOrOrg.assumeCommaInPersonName", value = "")
@SystemProperty(key = "dataverse.personOrOrg.orgPhraseArray", value = "")
class PersonOrOrgUtilTest {
    
    @ParameterizedTest
    @NullAndEmptySource
    void testRecognizingFailsForQuestionableInput(String sut) {
        // Null input is a special case, as we use NullSafeJsonBuilders
        String expected = sut == null ? "" : sut;
        
        JsonObject result = PersonOrOrgUtil.getPersonOrOrganization(sut, false, false);
        assertTrue(result.getBoolean("isPerson"), "isPerson=true failed for " + JsonUtil.prettyPrint(result));
        assertEquals(expected, result.getString("fullName"), "fullName is not " + expected + " but " + JsonUtil.prettyPrint(result));
    }
    
    @ParameterizedTest
    @ValueSource(strings = {
        // Simple cases
        "IBM", "Harvard University",
        
        // Examples with more complexity
        "The Institute for Quantitative Social Science", "Council on Aging",
        "The Ford Foundation", "United Nations Economic and Social Commission for Asia and the Pacific (UNESCAP)",
        "Michael J. Fox Foundation for Parkinson's Research",
        "Deanery Garden", "Harris Garden", "Wellford Park", "Christ's College", "Jodrell Bank Arboretum",
        
        // No comma should stop this being recognized as organizations
        "Digital Archive of Massachusetts Anti-Slavery and Anti-Segregation Petitions, Massachusetts Archives, Boston MA",
        "U.S. Department of Commerce, Bureau of the Census, Geography Division",
        "Harvard Map Collection, Harvard College Library",
        "Geographic Data Technology, Inc. (GDT)"
    })
    void testRecognizingAsOrganization(String sut) {
        JsonObject result = PersonOrOrgUtil.getPersonOrOrganization(sut, false, false);
        assertEquals(sut, result.getString("fullName"), () -> "Could not find fullName attribute as " + sut + " in " + JsonUtil.prettyPrint(result));
        assertFalse(result.getBoolean("isPerson"), "isPerson=false failed for " + JsonUtil.prettyPrint(result));
    }
    
    
    void testRecognizingDependingOnOrgPhrases(String sut, List<String> orgPhrases) {
    
    }
    
        /*

        @Test
        public void testOrganizationCOMPLEXName() {
            // The next example is one known to be asserted to be a Person without an entry
            // in the OrgWordArray
            // So we test with it in the array and then when the array is empty to verify
            // the array works, resetting the array works, and the problem still exists in
            // the underlying algorithm
            PersonOrOrgUtil.setOrgPhraseArray("[\"Portable\"]");
            verifyIsOrganization("Portable Antiquities of the Netherlands");
            PersonOrOrgUtil.setOrgPhraseArray(null);
            JsonObject obj = PersonOrOrgUtil.getPersonOrOrganization("Portable Antiquities of the Netherlands", false, false);
            assertTrue(obj.getBoolean("isPerson"));
        }
        
    @ParameterizedTest
    void testOrganizationAcademicName(String ) {
        verifyIsOrganization("John Smith Center");
        verifyIsOrganization("John Smith Group");
        //An example the base algorithm doesn't handle:
        PersonOrOrgUtil.setAssumeCommaInPersonName(true);
        verifyIsOrganization("John Smith Project");
        PersonOrOrgUtil.setAssumeCommaInPersonName(false);
    }
    
         */

        
        @Test
        public void testOrganizationCommaOrDash() {
            verifyIsOrganization("Digital Archive of Massachusetts Anti-Slavery and Anti-Segregation Petitions, Massachusetts Archives, Boston MA");
            verifyIsOrganization("U.S. Department of Commerce, Bureau of the Census, Geography Division");
            verifyIsOrganization("Harvard Map Collection, Harvard College Library");
            verifyIsOrganization("Geographic Data Technology, Inc. (GDT)");
        }

        @Disabled
        @Test
        public void testOrganizationES() {
            //Spanish recognition is not enabled - see export/Organization.java
            verifyIsOrganization("Compañía de San Fernando");
        }
        
        /**
         * Name is composed of:
         * <First Names> <Family Name>
         */
        @Test
        public void testName() {
            verifyIsPerson("Jorge Mario Bergoglio", "Jorge Mario", "Bergoglio");
            verifyIsPerson("Bergoglio", null, null);
            verifyIsPerson("Francesco Cadili", "Francesco", "Cadili");
            // This Philip Seymour Hoffman example is from ShibUtilTest.
            verifyIsPerson("Philip Seymour Hoffman", "Philip Seymour", "Hoffman");

            // test Smith (is also a name)
            verifyIsPerson("John Smith", "John", "Smith");
            // resolved using hint file
            verifyIsPerson("Guido van Rossum", "Guido", "van Rossum");
            // test only name
            verifyIsPerson("Francesco", "Francesco", null);
            // test only family name
            verifyIsPerson("Cadili", null, null);
            
            verifyIsPerson("kcjim11, kcjim11", "kcjim11", "kcjim11");
            
            verifyIsPerson("Bartholomew 3, James", "James", "Bartholomew 3");
            verifyIsPerson("Smith, ", null, "Smith");
            verifyIsPerson("Smith,", null, "Smith");
        }
        
        private void verifyIsOrganization(String fullName) {
            JsonObject obj = PersonOrOrgUtil.getPersonOrOrganization(fullName, false, false);
            System.out.println(JsonUtil.prettyPrint(obj));
            assertEquals(obj.getString("fullName"),fullName);
            assertFalse(obj.getBoolean("isPerson"));

        }
        
        private void verifyIsPerson(String fullName, String givenName, String familyName) {
            verifyIsPerson(fullName, givenName, familyName, false);
        }
        
        private void verifyIsPerson(String fullName, String givenName, String familyName, boolean isPerson) {
            JsonObject obj = PersonOrOrgUtil.getPersonOrOrganization(fullName, false, isPerson);
            System.out.println(JsonUtil.prettyPrint(obj));
            assertEquals(obj.getString("fullName"), StringUtil.normalize(fullName));
            assertTrue(obj.getBoolean("isPerson"));
            assertEquals(obj.containsKey("givenName"), givenName != null);
            if(obj.containsKey("givenName") && givenName != null) {
                assertEquals(obj.getString("givenName"),givenName);
            }
            assertEquals(obj.containsKey("familyName"), familyName != null);
            if(obj.containsKey("familyName") && familyName != null) {
                assertEquals(obj.getString("familyName"),familyName);
            }
        }

    }
