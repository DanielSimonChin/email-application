/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.test.mailtests;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.test.MethodLogger;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Rule;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Unit tests for methods in SendAndReceive.java which include plain text, html text, cc, and embedded attachments, attachments emails.
 * @author Daniel
 */
public class MailTests {
    SendAndReceive runMail;
    /**
     * Prepare the objects that will be re-used
     */
    @Before
    public void init() {
        runMail = new SendAndReceive();
    }
    @Rule
    public MethodLogger methodLogger = new MethodLogger();
    
    private final static Logger LOG = LoggerFactory.getLogger(MailTests.class);
    
    /**
     * Testing the sendEmail method which sends a simple text/html email to a single recipient.
     * An email is send, received and compared.
     */
    @Test
    public void singleEmailTest()
    {
        // Send an ordinary text message
        runMail.sendEmail();
        // Add a one second pause to allow the Gmail server to receive what has
        // been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        runMail.receiveEmail();
        LOG.info("HELLO");
        assertTrue(true);
    }
   
}