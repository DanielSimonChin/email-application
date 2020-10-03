package com.danielsimonchin.test.mailtests;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Flags;
import jodd.mail.Email;
import jodd.mail.EmailAddress;
import jodd.mail.EmailFilter;
import jodd.mail.EmailMessage;
import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;
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
 * @version September 20, 2020
 */
@Ignore
public class MailTests {

    private SendAndReceive runMail;
    private String plainMsg = "Hello from plain text email: " + LocalDateTime.now();
    private String htmlMsg = "<html><META http-equiv=Content-Type "
            + "content=\"text/html; charset=utf-8\">"
            + "<body><h1>Here is my photograph embedded in "
            + "this email.</h1><img src='cid:FreeFall.jpg'>"
            + "<h2>I'm flying!</h2></body></html>";
    private String subject = "Jodd Test";
    private MailConfigBean mailConfigBean;

    //List of all recipients, will be used later when validating the emails received
    private List<MailConfigBean> allRecipients;
    //The mail config beans relating to the recipients of the email. The fields left as 0 or null are those relating to database that are not part of the email object.
    private MailConfigBean recipient1 = new MailConfigBean(null,"danieldawsontest2@gmail.com", "Danieltester2","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);
    private MailConfigBean recipient2 = new MailConfigBean(null,"danieldawsontest3@gmail.com", "Danieltester3","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);
    private MailConfigBean recipient3 = new MailConfigBean(null,"recievedanieldawson1@gmail.com", "Danieltester4","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);

    //ArrayList of recipients in the To List
    private List<String> toList;
    //ArrayList of recipients in the CC List
    private List<String> ccList;
    //ArrayList of recipients in the BCC List
    private List<String> bccList;

    //ArrayList of attachment files
    private ArrayList<File> regularAttachments;
    //ArrayList of embedded attachment files
    private ArrayList<File> embeddedAttachments;

    /**
     * Before every test, re-instantiate all the lists and variables that will
     * be re-used in tests.
     */
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
        this.mailConfigBean = new MailConfigBean(null,"danieldawsontest1@gmail.com", "Danieltester1","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);

        //This is to make sure that all emails in all recipient accounts have seen/read the emails sent. Prevents having a test affect the result of the next.
        allRecipients.stream().map(recipient -> MailServer.create()
                .host(recipient.getImapUrl())
                .ssl(true)
                .auth(recipient.getUserEmailAddress(), recipient.getPassword())
                .buildImapMailServer()).forEachOrdered(imapServer -> {
            try ( ReceiveMailSession session = imapServer.createSession()) {
                session.open();
                //read all the emails and mark them as seen
                ReceivedEmail[] receivedEmails = session.receiveEmailAndMarkSeen(EmailFilter.filter().flag(Flags.Flag.SEEN, false));
            }
        });
    }

    /**
     * Testing the sendEmail method which sends an email to multiple recipients.
     * An email is sent, received and compared. Assuming that all the input
     * parameters are valid and provided.
     */
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
                    //Check that the subject line matches
                    if (resultEmail.subject().equals(email.subject())) {
                        //Checking that the cc lists of recipients are identical
                        if (compareRecipientArrays(resultEmail.cc(), email.cc())) {
                            List<EmailMessage> messages = email.messages();
                            //the messages coming from the returned Email object
                            List<EmailMessage> resultEmailMessages = resultEmail.messages();
                            //Check that the plain text and html message matches
                            if (resultEmailMessages.get(0).getContent().equals(messages.get(0).getContent()) && resultEmailMessages.get(1).getContent().equals(messages.get(1).getContent())) {
                                passedEmailsCount++;
                            }
                        }
                    }
                }
            }
        }
        //Test that the number of valid emails is equal to the amount of emails sent.
        assertEquals(allRecipients.size(), passedEmailsCount);
    }

    /**
     * Helper method that checks that two lists of email addresses are
     * identical. Useful to see that the to,cc,bcc fields are the same
     *
     * @param list1 First List of recipients
     * @param list2 Second List of recipients
     * @return true if their contents are equals, false otherwise.
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
    @Test
    public void sendEmailToList() {
        //Adding people into the TO list
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
        assertEquals(toList.size(), countValidRecipients);
    }

    /**
     * This sends an email to the CC List only. The TO and BCC fields are left
     * empty. Checking that the CCFields are the same, and the TO, BCC are empty
     */
    @Test
    public void sendEmailCCList() {
        //Adding people into the TO list
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

        //The recipients of the cc list from the returned Email object
        EmailAddress[] resultEmailCC = resultEmail.cc();
        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                //The To list of every receivedEmail
                EmailAddress[] receivedEmailCC = email.cc();
                if (resultEmailCC.length == receivedEmailCC.length) {
                    if (compareRecipientArrays(resultEmailCC, receivedEmailCC)) {
                        //increment if the to and bcc are empty and the previous condition passed
                        if (email.to().length == 0 && resultEmail.bcc().length == 0) {
                            //increment the number of passed emails for the TO list recipients
                            countValidRecipients++;
                        }
                    }
                }
            }
        }
        assertEquals(ccList.size(), countValidRecipients);
    }

    /**
     * Sends an email to the bcc List only. Checking that the bcc fields are the
     * same, and the TO, CC are empty.
     */
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

        //Check that the count of bcc recipients is the same as count of sent emails
        if (resultEmail.bcc().length == bccList.size()) {
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
        }
        assertEquals(bccList.size(), countBccEmailsSent);
    }

    /**
     * Sends an email and verifies the contents of the attachments. Loops
     * through every email, compares the file names and if one of the files are
     * embedded.
     */
    @Test
    public void testAllAttachments() {
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
        int countValidRecipients = 0;

        for (MailConfigBean recipient : allRecipients) {
            //The email that this recipient just received
            ReceivedEmail[] emails = runMail.receiveEmail(recipient);
            for (ReceivedEmail email : emails) {
                //Comparing the names to the expected file names
                if (regularAttachments.get(0).getName().equals(email.attachments().get(1).getName()) && embeddedAttachments.get(0).getName().equals(email.attachments().get(0).getName())) {
                    //One attachment must be regular and the other is an embedded attachment
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
     * invalid recipient email addresses.
     */
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
     * Expects an IllegalArgumentException if the sender's bean email address is
     * invalid (invalid format). The check is made in the sendEmail method.
     */
    @Test(expected = IllegalArgumentException.class)
    public void testInvalidMailConfigBeanUserName() {
        MailConfigBean invalidSenderBean = new MailConfigBean(null,"INVALIDSENDER", "Danieltester1","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);
        toList.add(recipient1.getUserEmailAddress());
        runMail = new SendAndReceive(invalidSenderBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        //fail the test if the program does not throw an IllegalArgumentException
        fail("The program did not receive an invalid sender MailConfigBean");
    }

    /**
     * This test verifies that the number of emails sent is the same number of
     * emails received in receiveEmail. (5 emails sent and received)
     */
    @Test
    public void testReceiveEmail() {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        for (int i = 0; i < 5; i++) {
            //send 5 emails 
            Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);
        }
        // Add a three second pause to allow the Gmail server to receive what has been sent
        try {
            Thread.sleep(3000);
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
        }
        int emailCount = 0;
        //The emails that this recipient just received
        ReceivedEmail[] emails = runMail.receiveEmail(recipient1);
        for (ReceivedEmail email : emails) {
            if (email.from().toString().equals(mailConfigBean.getUserEmailAddress())) {
                emailCount++;
            }
        }
        assertEquals(5, emailCount);
    }

    /**
     * Tests that the receiveEmail method returns an empty array when there are
     * no emails to receive.
     */
    @Test
    public void testNoEmailsSent() {
        runMail = new SendAndReceive(mailConfigBean);
        ReceivedEmail[] emails = runMail.receiveEmail(recipient1);
        //Assert that the number of emails in the result array is 0
        assertEquals(0, emails.length);
    }

    /**
     * Tests that the to and cc lists of the receivedEmail[] are the same as the
     * intended recipients.
     */
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
     * invalid and cannot properly authenticate.
     */
    @Test
    public void receiveEmailInvalidUsername() {
        MailConfigBean invalidUser = new MailConfigBean(null,"invalidUserName@gmail.com", "Danieltester2","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);
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
     * invalid and cannot properly authenticate.
     */
    @Test
    public void receiveEmailInvalidPassword() {
        MailConfigBean invalidUser = new MailConfigBean(null,"danieldawsontest2@gmail.com", "invalidpassword2020","imap.gmail.com","smtp.gmail.com",null,null,null,null,null,null,null);
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
     * Checks that the receiveEmail method returns a null if the host is invalid
     * and cannot properly authenticate.
     */
    @Test
    public void receiveEmailInvalidHost() {
        //This bean does not have a valid recipient host (should be imap.gmail.com)
        MailConfigBean invalidUser = new MailConfigBean(null,"danieldawsontest2@gmail.com","Danieltester2","invalidHost.com","smtp.gmail.com",null,null,null,null,null,null,null);
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
        Assert.assertArrayEquals(null, emails);
    }
}
