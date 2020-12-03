package edu.harvard.iq.dataverse.util;

import com.jayway.restassured.RestAssured;
import com.jayway.restassured.parsing.Parser;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import javax.mail.Message;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;

import java.io.PrintWriter;
import java.util.Date;

import static com.jayway.restassured.RestAssured.given;
import static org.hamcrest.CoreMatchers.equalTo;

@Tag("testcontainers")
@Testcontainers
class MailSessionProducerIT {
    
    private static final Integer PORT_SMTP = 1025;
    private static final Integer PORT_HTTP = 8025;
    
    Integer smtpPort;
    String smtpHost;
    
    MailSessionProducer mailSessionProducer = new MailSessionProducer();
    
    @Container
    public static GenericContainer<?> mailhog = new GenericContainer<>("mailhog/mailhog")
        .withExposedPorts(PORT_HTTP, PORT_SMTP)
        .waitingFor(Wait.forHttp("/"));
    
    @BeforeEach
    public void setUp() {
        smtpPort = mailhog.getMappedPort(PORT_SMTP);
        smtpHost = mailhog.getContainerIpAddress();
        Integer httpPort = mailhog.getMappedPort(PORT_HTTP);
        
        RestAssured.baseURI = "http://" + smtpHost;
        RestAssured.port = httpPort;
        RestAssured.basePath = "/api/v2";
        RestAssured.registerParser("text/json", Parser.JSON);
        
        System.setProperty("dataverse.mail.system.host", smtpHost);
        System.setProperty("dataverse.mail.system.port", smtpPort.toString());
    }
    
    @Test
    void testMailSession() {
        // given
        Session mailSession = mailSessionProducer.getSystemMailSession();
        
        // when
        sendEmail(mailSession, "foobar@example.org", "foobar", "foobar");
        
        // then
        given().get("/messages").then()
            .statusCode(200)
            .body("total", equalTo(1));
    }
    
    public static void sendEmail(Session session, String toEmail, String subject, String body){
        try
        {
            MimeMessage msg = new MimeMessage(session);
            //set message headers
            msg.addHeader("Content-type", "text/HTML; charset=UTF-8");
            msg.addHeader("format", "flowed");
            msg.addHeader("Content-Transfer-Encoding", "8bit");
            msg.setReplyTo(InternetAddress.parse("no_reply@example.com", false));
            msg.setSubject(subject, "UTF-8");
            msg.setText(body, "UTF-8");
            msg.setSentDate(new Date());
            msg.setRecipients(Message.RecipientType.TO, InternetAddress.parse(toEmail, false));
            
            Transport.send(msg);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
    }
    
}