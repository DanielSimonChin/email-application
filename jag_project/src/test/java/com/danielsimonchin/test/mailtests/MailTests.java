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
import jodd.mail.EmailAddress;
import jodd.mail.EmailMessage;
import jodd.mail.ReceivedEmail;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;
import org.junit.Ignore;

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
    private MailConfigBean recipient1 = new MailConfigBean("imap.gmail.com", "danieldawsontest2@gmail.com", "Danieltester2");
    private MailConfigBean recipient2 = new MailConfigBean("imap.gmail.com", "danieldawsontest3@gmail.com", "Danieltester3");
    private MailConfigBean recipient3 = new MailConfigBean("imap.gmail.com", "recievedanieldawson1@gmail.com", "Danieltester4");

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
        allRecipients.add(recipient1);
        allRecipients.add(recipient2);
        allRecipients.add(recipient3);
        regularAttachments = new ArrayList<>();
        embeddedAttachments = new ArrayList<>();
        this.mailConfigBean = new MailConfigBean("smtp.gmail.com", "danieldawsontest1@gmail.com", "Danieltester1");
    }

    /**
     * Testing the sendEmail method which sends a simple text/html email to a
     * single recipient. An email is sent, received and compared. Assuming that
     * all the input parameters are valid and provided
     */
    @Ignore
    @Test
    public void sendEmailTestPassed() {

        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(recipient3.getUserEmailAddress());
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

    /**
     * This sends an email to the TO List only. The cc and bcc fields are left
     * empty. Checking that the toFields are the same, and the cc, bcc are empty
     */
    @Ignore
    @Test
    public void sendEmailToList() {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        toList.add(recipient2.getUserEmailAddress());
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
        int countValidRecipients = 0;

        //The recipients of the To list from the returned Email object
        EmailAddress[] resultEmailTo = resultEmail.to();
        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                //The To list of every receivedEmail
                EmailAddress[] receivedEmailTo = email.to();
                if (resultEmailTo.length == receivedEmailTo.length) {
                    if (compareRecipientArrays(resultEmailTo, receivedEmailTo)) {
                        //increment if the cc and bcc are empty and the previous condition passed
                        if (email.cc().length == 0 && resultEmail.bcc().length == 0) {
                            //increment the number of passed emails for the TO list recipients
                            countValidRecipients++;
                        }
                    }
                }
            }
        }
        System.out.println();
        assertEquals(toList.size(), countValidRecipients);
    }

    /**
     * This sends an email to the CC List only. The TO and BCC fields are left
     * empty. Checking that the toFields are the same, and the TO, BCC are empty
     */
    @Ignore
    @Test
    public void sendEmailCCList() {
        //Adding one person into the TO list
        ccList.add(recipient1.getUserEmailAddress());
        ccList.add(recipient2.getUserEmailAddress());
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
        int countValidRecipients = 0;

        //The recipients of the To list from the returned Email object
        EmailAddress[] resultEmailCC = resultEmail.cc();
        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                //The To list of every receivedEmail
                EmailAddress[] receivedEmailCC = email.cc();
                if (resultEmailCC.length == receivedEmailCC.length) {
                    if (compareRecipientArrays(resultEmailCC, receivedEmailCC)) {
                        //increment if the cc and bcc are empty and the previous condition passed
                        if (email.to().length == 0 && resultEmail.bcc().length == 0) {
                            //increment the number of passed emails for the TO list recipients
                            countValidRecipients++;
                        }
                    }
                }
            }
        }
        System.out.println();
        assertEquals(ccList.size(), countValidRecipients);
    }

    /**
     * Sends an email to the bcc List only. The TO and cc fields are left
     */
    @Ignore
    @Test
    public void sendEmailBCCList() {
        //Adding people into the TO list
        bccList.add(recipient1.getUserEmailAddress());
        bccList.add(recipient2.getUserEmailAddress());
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

        int countBccEmailsSent = 0;
        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);

            //Since the ReceivedEmail class doesn't have a bcc() method, we make sure the other fields are empty and increment the number of bcc emails
            for (ReceivedEmail email : emails) {
                if (email.to().length == 0 && email.cc().length == 0) {
                    countBccEmailsSent++;
                }
            }
        }
        System.out.println();
        assertEquals(bccList.size(), countBccEmailsSent);
    }

    @Ignore
    @Test
    public void testAllAttachments() {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
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
        int countValidRecipients = 0;

        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                if (regularAttachments.get(0).getName().equals(email.attachments().get(1).getName()) && embeddedAttachments.get(0).getName().equals(email.attachments().get(0).getName())) {
                    if (email.attachments().get(0).isEmbedded() && !email.attachments().get(1).isEmbedded()) {
                        countValidRecipients++;
                    }

                }
            }
        }
        assertEquals(toList.size(), countValidRecipients);
    }

    /**
     * Expects to receive an IllegalArgumentException since no recipients were
     * given in the parameters for sendEmail
     */
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testNoRecipients() {
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //fail the test if the program does not throw an IllegalArgumentException
        fail("The parameters were given at least 1 recipient");
    }

    /**
     * Expects to receive a NullPointerException when presented with a null
     * email address
     */
    @Ignore
    @Test(expected = NullPointerException.class)
    public void testNullEmailAddress() {
        toList.add(recipient1.getUserEmailAddress());
        toList.add(null);
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //fail the test if the program does not throw an NullPointerException
        fail("The parameters were not given a null email address");
    }

    /**
     * Expects to receive an IllegalArgumentException when presented with an
     * invalid email addresses.
     */
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidEmailAddress() {
        toList.add(recipient1.getUserEmailAddress());
        toList.add("FakeEmailAddressString");
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //fail the test if the program does not throw an NullPointerException
        fail("The parameters did not receive an invalid email address");
    }

    /**
     * This test verifies that the number of emails sent is the same number of
     * emails received in receiveEmail.
     */
    @Ignore
    @Test
    public void testReceiveEmail() {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(recipient3.getUserEmailAddress());
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
        int emailCount = 0;
        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            emailCount += emails.length;
        }
        int totalEmailsSent = toList.size() + ccList.size() + bccList.size();
        assertEquals(totalEmailsSent, emailCount);
    }

    /**
     * This unit test verifies that the receivedEmail object has the correct
     * sender
     */
    @Ignore
    @Test
    public void testReceiveEmailVerifySender() {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
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
        ReceivedEmail[] emails = runMail.receiveEmail(recipient1);
        //Pass the test if the sender's email is the same as the email's sender
        assertEquals(mailConfigBean.getUserEmailAddress(), emails[0].from().toString());
    }

    /**
     * Tests that the to and cc lists of the receivedEmail[] are the same as the
     * intended recipients.
     */
    @Ignore
    @Test
    public void receiveEmailCheckRecipientNames() {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding two people into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        ccList.add(recipient3.getUserEmailAddress());
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        // Add a three second pause to allow the Gmail server to receive what has been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        int validEmails = 0;

        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                if (compareRecipientArrays(resultEmail.to(), email.to())) {
                    if (compareRecipientArrays(resultEmail.cc(), email.cc())) {
                        validEmails++;
                    }
                }
            }
        }
        int totalEmailsSent = toList.size() + ccList.size();
        assertEquals(totalEmailsSent, validEmails);
    }

    /**
     * Checks that the receiveEmail method returns a null if the username is
     * invalid. The program is expected to fail.
     */
    @Ignore
    @Test
    public void receiveEmailInvalidUsername() {
        MailConfigBean invalidUser = new MailConfigBean("imap.gmail.com", "invalidusername123@gmail.com", "Danieltester2");
        toList.add(invalidUser.getUserEmailAddress());
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        // Add a three second pause to allow the Gmail server to receive what has been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        ReceivedEmail[] emails = runMail.receiveEmail(invalidUser);
        //The returned array is null since the username is invalid
        Assert.assertArrayEquals(null, emails);
    }

    /**
     * Checks that the receiveEmail method returns a null if the password is
     * invalid. The program is expected to fail.
     */
    @Ignore
    @Test
    public void receiveEmailInvalidPassword() {
        MailConfigBean invalidUser = new MailConfigBean("imap.gmail.com", "danieldawsontest2@gmail.com", "invalidpassword2020");
        toList.add(invalidUser.getUserEmailAddress());
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        // Add a three second pause to allow the Gmail server to receive what has been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        ReceivedEmail[] emails = runMail.receiveEmail(invalidUser);
        //The returned array is null since the password is invalid
        Assert.assertArrayEquals(null, emails);
    }

    /**
     * Expects an IllegalArgumentException if the host name of the receiver is
     * the same as the sender host name.
     */
    @Test(expected = IllegalArgumentException.class)
    public void receiveEmailInvalidHost() {
        MailConfigBean invalidUser = new MailConfigBean("smtp.gmail.com", "danieldawsontest2@gmail.com", "Danieltester2");
        toList.add(invalidUser.getUserEmailAddress());
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        // Add a three second pause to allow the Gmail server to receive what has been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        ReceivedEmail[] emails = runMail.receiveEmail(invalidUser);
        fail("The host was valid");
    }

}
