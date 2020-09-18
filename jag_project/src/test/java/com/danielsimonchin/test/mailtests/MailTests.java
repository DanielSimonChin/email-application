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
import javax.activation.DataSource;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailAttachment;
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

    private SendAndReceive runMail;
    private final String plainMsg = "Hello from plain text email: " + LocalDateTime.now();
    private final String htmlMsg = "<html><META http-equiv=Content-Type "
            + "content=\"text/html; charset=utf-8\">"
            + "<body><h1>HTML Message</h1>"
            + "<h2>Here is some text in the HTML message</h2></body></html>";
    private final String subject = "Jodd Test";
    private MailConfigBean mailConfigBean;

    //List of all recipients, will be used later when validating the emails received
    private List<MailConfigBean> allRecipients = new ArrayList<>();
    //The mail config beans relating to the recipients of the email
    private MailConfigBean toListRecipient = new MailConfigBean("imap.gmail.com", "danieldawsontest2@gmail.com", "Danieltester2");
    private MailConfigBean ccListRecipient = new MailConfigBean("imap.gmail.com", "danieldawsontest3@gmail.com", "Danieltester3");
    private MailConfigBean bccListRecipient = new MailConfigBean("imap.gmail.com", "recievedanieldawson1@gmail.com", "Danieltester4");

    //ArrayList of recipients in the To List
    private List<String> toList = new ArrayList<>();
    //ArrayList of recipients in the CC List
    private List<String> ccList = new ArrayList<>();
    //ArrayList of recipients in the BCC List
    private List<String> bccList = new ArrayList<>();

    //ArrayList of attachment files
    private ArrayList<File> regularAttachments = new ArrayList<>();
    //ArrayList of embedded attachment files
    private ArrayList<File> embeddedAttachments = new ArrayList<>();

    @Before
    public void createMailConfigBean() {
        allRecipients = new ArrayList<>();
        toList = new ArrayList<>();
        ccList = new ArrayList<>();
        bccList = new ArrayList<>();
        allRecipients.add(toListRecipient);
        allRecipients.add(ccListRecipient);
        allRecipients.add(bccListRecipient);
        regularAttachments = new ArrayList<>();
        embeddedAttachments = new ArrayList<>();
        this.mailConfigBean = new MailConfigBean("smtp.gmail.com", "danieldawsontest1@gmail.com", "Danieltester1");
    }

    /**
     * Testing the sendEmail method which sends a simple text/html email to a
     * single recipient. An email is sent, received and compared.
     */
    @Test
    public void sendEmailTestPassed1() {

        //Adding one person into the TO list
        toList.add(toListRecipient.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(ccListRecipient.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(bccListRecipient.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
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
                if (resultEmail.from().toString().equals(email.from().toString())) {

                    System.out.println(resultEmail.from().toString() + " " + email.from().toString());
                    //Check that the subject line matches
                    if (resultEmail.subject().equals(email.subject())) {
                        System.out.println(resultEmail.subject() + " " + email.subject());

                        //Checking that the cc lists of recipients are identical
                        if (compareRecipientArrays(resultEmail.cc(), email.cc())) {

                            List<EmailMessage> messages = email.messages();
                            //the messages coming from the returned Email object
                            List<EmailMessage> resultEmailMessages = resultEmail.messages();
                            //Check that the plain text and html message matches
                            if (resultEmailMessages.get(0).getContent().equals(messages.get(0).getContent()) && resultEmailMessages.get(1).getContent().equals(messages.get(1).getContent())) {
                                System.out.println(resultEmailMessages.get(0).getContent() + " : " + messages.get(0).getContent());
                                System.out.println(resultEmailMessages.get(1).getContent() + " : " + messages.get(1).getContent());
                                passedEmailsCount++;

                                /*List<String> EmailAttachmentNames = new ArrayList<>();
                                List<String> receivedEmailAttachmentNames = new ArrayList<>();
                                List<EmailAttachment<? extends DataSource>> attachments1 = email.attachments();
                                email.
                                
                                if (attachments1 != null) {
                                    attachments1.stream().map((attachment) -> {
                                        EmailAttachmentNames.add(attachment.getName());
                                        return attachment;
                                    });
                                }
                                List<EmailAttachment<? extends DataSource>> attachments2 = resultEmail.attachments();
                                if (attachments2 != null) {
                                    attachments2.stream().map((attachment) -> {
                                        System.out.println(attachment.getName());
                                        receivedEmailAttachmentNames.add(attachment.getName());
                                        return attachment;
                                    });
                                }
                                System.out.println(EmailAttachmentNames.size() + " : " + EmailAttachmentNames.size());
                                    System.out.println(receivedEmailAttachmentNames.get(1) + " : " + receivedEmailAttachmentNames.get(1));
                                if (EmailAttachmentNames.get(0).equals(EmailAttachmentNames.get(0)) && receivedEmailAttachmentNames.get(1).equals(receivedEmailAttachmentNames.get(1))) {
                                    System.out.println(EmailAttachmentNames.get(0) + " : " + EmailAttachmentNames.get(0));
                                    System.out.println(receivedEmailAttachmentNames.get(1) + " : " + receivedEmailAttachmentNames.get(1));
                                    //Pass this email and consider it valid only if all the previous conditions are met.
                                    passedEmailsCount++;
                                }*/

                            }
                        }
                    }
                }
            }
            System.out.println();
        }
        //Test that the number of valid emails is equal to the amount of emails sent.
        assertEquals(allRecipients.size(), passedEmailsCount);
    }

    /**
     * Checks that two lists of email addresses are identical. Useful to see
     * that the cc fields are the same
     *
     * @param list1 First List of cc recipients
     * @param list2 Second List of bcc recipients
     * @return
     */
    private boolean compareRecipientArrays(EmailAddress[] list1, EmailAddress[] list2) {
        int countPassed = 0;
        if (list1.length == list2.length) {
            int index = 0;
            for (EmailAddress emailAddress : list1) {
                if (emailAddress.toString().equals(list2[index].toString())) {
                    countPassed++;
                    index++;
                }
            }
        }
        return countPassed == list1.length;
    }

}
