package edu.harvard.iq.dataverse.util;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class MailSessionProducerTest {
    
    @BeforeAll
    static void setUp() {
        System.setProperty(MailSessionProducer.MAIL_CONFIG_PREFIX+".test", "test");
    }
    
    @Test
    void getMailProperties() {
        MailSessionProducer test = new MailSessionProducer();
        assertEquals(1, test.getMailProperties().stringPropertyNames().size());
        assertTrue(test.getMailProperties().stringPropertyNames().contains("mail.smtp.test"));
    }
}