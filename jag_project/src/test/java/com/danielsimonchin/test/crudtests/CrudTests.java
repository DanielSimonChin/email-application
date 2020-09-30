/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.test.crudtests;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.persistence.EmailDAOImpl;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import javax.mail.Flags;
import jodd.mail.Email;
import jodd.mail.EmailFilter;
import jodd.mail.MailServer;
import jodd.mail.ReceiveMailSession;
import jodd.mail.ReceivedEmail;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;

/**
 *
 * @author Daniel
 */
public class CrudTests {

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
    private MailConfigBean recipient1 = new MailConfigBean(null, "danieldawsontest2@gmail.com", "Danieltester2", "imap.gmail.com", "smtp.gmail.com", null, null, null, null, null, null, null);
    private MailConfigBean recipient2 = new MailConfigBean(null, "danieldawsontest3@gmail.com", "Danieltester3", "imap.gmail.com", "smtp.gmail.com", null, null, null, null, null, null, null);
    private MailConfigBean recipient3 = new MailConfigBean(null, "recievedanieldawson1@gmail.com", "Danieltester4", "imap.gmail.com", "smtp.gmail.com", null, null, null, null, null, null, null);

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
        String databaseURL = "jdbc:mysql://localhost:3306?zeroDateTimeBehavior=CONVERT_TO_NULL";
        this.mailConfigBean = new MailConfigBean("Daniel", "danieldawsontest1@gmail.com", "Danieltester1", "imap.gmail.com", "smtp.gmail.com", "993", "465", databaseURL, "EMAILCLIENT", "3306", "root", "logitech7790");

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

    @Test
    public void testEmailCreate() throws SQLException {
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
        EmailDAOImpl crud = new EmailDAOImpl(mailConfigBean);
        int result = crud.create(resultEmail);
        assertEquals(1,result);
    }

}
