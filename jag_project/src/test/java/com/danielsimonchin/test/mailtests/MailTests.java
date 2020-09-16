/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.test.mailtests;

import com.danielsimonchin.business.SendAndReceive;
import data.MailConfigBean;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import jodd.mail.Email;
import jodd.mail.EmailMessage;
import jodd.mail.ReceivedEmail;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 * Unit tests for methods in SendAndReceive.java which include plain text, html
 * text, cc, and embedded attachments, attachments emails.
 *
 * @author Daniel Simon Chin
 */
public class MailTests {

    SendAndReceive runMail;
    private final String plainMsg = "Hello from plain text email: " + LocalDateTime.now();
    private final String htmlMsg = "<html><META http-equiv=Content-Type "
            + "content=\"text/html; charset=utf-8\">"
            + "<body><h1>HTML Message</h1>"
            + "<h2>Here is some text in the HTML message</h2></body></html>";
    private final String subject = "Jodd Test";
    MailConfigBean mailConfigBean;

    @Before
    public void createMailConfigBean() {
        this.mailConfigBean = new MailConfigBean("smtp.gmail.com", "danieldawsontest1@gmail.com", "Danieltester1");
    }

    /**
     * Testing the sendEmail method which sends a simple text/html email to a
     * single recipient. An email is sent, received and compared.
     */
    @Test
    public void sendEmailTest() {
        //List of all recipients, will be used later when validating the emails received
        ArrayList<MailConfigBean> allRecipients = new ArrayList<>();
        //The mail config beans relating to the recipients of the email
        MailConfigBean toListRecipient = new MailConfigBean("imap.gmail.com", "danieldawsontest2@gmail.com", "Danieltester2");
        MailConfigBean ccListRecipient = new MailConfigBean("imap.gmail.com", "danieldawsontest3@gmail.com", "Danieltester3");
        MailConfigBean bccListRecipient = new MailConfigBean("imap.gmail.com", "recievedanieldawson1@gmail.com", "Danieltester4");

        allRecipients.add(toListRecipient);
        allRecipients.add(ccListRecipient);
        allRecipients.add(bccListRecipient);

        //ArrayList of recipients in the To List
        ArrayList<String> toList = new ArrayList<>();
        toList.add(toListRecipient.getUserEmailAddress());
        //ArrayList of recipients in the CC List
        ArrayList<String> ccList = new ArrayList<>();
        ccList.add(ccListRecipient.getUserEmailAddress());
        //ArrayList of recipients in the BCC List
        ArrayList<String> bccList = new ArrayList<>();
        bccList.add(bccListRecipient.getUserEmailAddress());
        //ArrayList of attachment files
        ArrayList<File> regularAttachments = new ArrayList<>();
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //ArrayList of embedded attachment files
        ArrayList<File> embeddedAttachments = new ArrayList<>();
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        // Add a three second pause to allow the Gmail server to receive what has been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        //Count of emails which have been sent and properly validated with the intended parameters
        int passedEmailsCount = 0;
        //Go through every recipient's inbox and validate their newly received email.
        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                //Check that the sender email address matches
                if (mailConfigBean.getUserEmailAddress().equals(email.from().toString())) {

                    System.out.println(mailConfigBean.getUserEmailAddress() + " " + email.from().toString());
                    //Check that the subject line matches
                    if (subject.equals(email.subject())) {

                        System.out.println(subject + " " + email.subject());
                        List<EmailMessage> messages = email.messages();
                        //Check that the plain text and html message matches
                        if (plainMsg.equals(messages.get(0).getContent()) && htmlMsg.equals(messages.get(1).getContent())) {
                            System.out.println(messages.get(0).getContent());
                            System.out.println(messages.get(1).getContent());
                            //Pass this email and consider it valid only if all the previous conditions are met.
                            passedEmailsCount++;
                        }
                    }
                }
            }
            System.out.println();
        }
        //Test that the number of valid emails is equal to the amount of emails sent.
        assertEquals(allRecipients.size(), passedEmailsCount);
    }
}
