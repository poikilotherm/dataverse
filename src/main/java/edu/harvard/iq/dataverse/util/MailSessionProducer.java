package edu.harvard.iq.dataverse.util;

import org.eclipse.microprofile.config.Config;
import org.eclipse.microprofile.config.ConfigProvider;

import javax.annotation.Resource;
import javax.enterprise.inject.Produces;
import javax.inject.Singleton;
import javax.mail.Session;
import java.util.Map;
import java.util.Properties;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

@Singleton
public class MailSessionProducer {
    
    public static final String MAIL_CONFIG_PREFIX = "dataverse.mail.system";
    
    @Produces
    @Resource(name = "java:app/notify/mail/system")
    public Session getSystemMailSession() {
        return Session.getInstance(getMailProperties());
    }
    
    Properties getMailProperties() {
        Config config = ConfigProvider.getConfig();
    
        // Map properties 1:1 to mail.smtp properties for the mail session.
        // See https://javaee.github.io/javamail/docs/api/com/sun/mail/smtp/package-summary.html for an extensive list
        // of options.
        Map<String,String> propMap = StreamSupport.stream(config.getPropertyNames().spliterator(), false)
                                                  .filter(prop -> prop.startsWith(MAIL_CONFIG_PREFIX+"."))
                                                  .collect(Collectors.toConcurrentMap(s -> s.replace(MAIL_CONFIG_PREFIX, "mail.smtp"), s -> config.getValue(s, String.class)));
        Properties mailProps = new Properties();
        mailProps.putAll(propMap);
        
        return mailProps;
    }
}
