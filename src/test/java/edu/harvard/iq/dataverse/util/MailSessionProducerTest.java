package edu.harvard.iq.dataverse.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import javax.mail.Session;

import static org.junit.jupiter.api.Assertions.*;

class MailSessionProducerTest {
    
    @BeforeEach
    void setUp() {
        System.setProperty(MailSessionProducer.MAIL_CONFIG_PREFIX+".test", "test");
    }
    
    @Test
    void getMailProperties() {
        MailSessionProducer test = new MailSessionProducer();
        assertEquals(1, test.getMailProperties().stringPropertyNames().size());
        assertTrue(test.getMailProperties().stringPropertyNames().contains("mail.smtp.test"));
    }
    
    @Test
    void testSessionCacheHits() {
        // given
        MailSessionProducer test = new MailSessionProducer();
        Session testSession = test.getSystemMailSession();
        
        //when
        assertEquals("test", testSession.getProperty("mail.smtp.test"));
        testSession = test.getSystemMailSession();
        
        // then
        assertEquals("test", testSession.getProperty("mail.smtp.test"));
    }
    
    @Test
    void testSessionCacheUpdates() {
        // given
        MailSessionProducer test = new MailSessionProducer();
        Session testSession = test.getSystemMailSession();
        
        //when
        assertEquals("test", testSession.getProperty("mail.smtp.test"));
        System.setProperty(MailSessionProducer.MAIL_CONFIG_PREFIX+".test", "test2");
        testSession = test.getSystemMailSession();
        
        // then
        assertEquals("test2", testSession.getProperty("mail.smtp.test"));
    }
}