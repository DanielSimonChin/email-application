/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.test.mailtests;

import com.danielsimonchin.business.SendAndReceive;
import data.MailConfig;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jodd.mail.EmailMessage;
import jodd.mail.ReceivedEmail;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for methods in SendAndReceive.java which include plain text, html text, cc, and embedded attachments, attachments emails.
 * @author Daniel
 */
public class MailTests {
    SendAndReceive runMail;
    String plainMsg = "Hello from plain text email: " + LocalDateTime.now();
    String htmlMsg = "<html><META http-equiv=Content-Type "
                            + "content=\"text/html; charset=utf-8\">"
                            + "<body><h1>HTML Message</h1>"
                            + "<h2>Here is some text in the HTML message</h2></body></html>";
    String subject = "Jodd Test";
    /**
     * Prepare the objects that will be re-used
     */
    @Before
    public void init() {
        runMail = new SendAndReceive();
    }
    
    private final static Logger LOG = LoggerFactory.getLogger(MailTests.class);
    
    /**
     * Testing the sendEmail method which sends a simple text/html email to a single recipient.
     * An email is sent, received and compared.
     */
    @Test
    public void singleEmailTest()
    {
        MailConfig sender = new MailConfig("smtp.gmail.com","danieldawsontest1@gmail.com","Danieltester1");
        ArrayList<MailConfig> recipients = new ArrayList<>();
        recipients.add(new MailConfig("smtp.gmail.com","danieldawsontest2@gmail.com","Danieltester2"));
        recipients.add(new MailConfig("smtp.gmail.com","danieldawsontest3@gmail.com","Danieltester3"));
        
        runMail.sendEmail(sender,recipients, subject, plainMsg, htmlMsg );
        // Add a three second pause to allow the Gmail server to receive what has
        // been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        
        int passedEmailsCount = 0;
        for(MailConfig recipient : recipients)
        {
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for(ReceivedEmail email : emails)
            {
                if(sender.getUserEmailAddress().equals(email.from().toString())){
                    System.out.println(sender.getUserEmailAddress() + " " + email.from().toString());
                    if(subject.equals(email.subject())){
                        System.out.println(subject + " " + email.subject());
                        passedEmailsCount++;
                    }
                        
                        
                     
                }
            }
        }
        
        assertEquals(recipients.size(),passedEmailsCount);
    }
}
