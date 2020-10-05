/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package com.danielsimonchin.test.crudtests;

import com.danielsimonchin.business.SendAndReceive;
import com.danielsimonchin.persistence.EmailDAO;
import com.danielsimonchin.persistence.EmailDAOImpl;
import com.danielsimonchin.properties.EmailBean;
import com.danielsimonchin.properties.MailConfigBean;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
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
import org.junit.Ignore;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author Daniel
 */
public class CrudTests {

    private final static Logger LOG = LoggerFactory.getLogger(CrudTests.class);

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
        String databaseURL = "jdbc:mysql://localhost:3306/EMAILCLIENT";
        this.mailConfigBean = new MailConfigBean("daniel", "danieldawsontest1@gmail.com", "Danieltester1", "imap.gmail.com", "smtp.gmail.com", "993", "465", databaseURL, "EMAILCLIENT", "3306", "daniel", "danielpw");

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
    public void testEmailCreate() throws SQLException, IOException {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //toList.add("notInTheAddressesTable@gmail.com");
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

        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //since we are sending it, it does not have a receivedDate so we leave it as null. The emailId will be set in the create method
        EmailBean emailBean = new EmailBean(0, 2, null, resultEmail);
        int result = crud.createEmailRecord(emailBean);
        assertEquals(1,result);
    }

    @Ignore
    @Test
    public void testFindAll() throws SQLException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailBeanList = crud.findAll();
        assertEquals("testFindAll: ", 8, emailBeanList.size());
    }
    @Ignore
    @Test
    public void testFindById() throws SQLException, IOException {
        //Adding one person into the TO list
        toList.add(recipient1.getUserEmailAddress());
        //Adding one person into the CC list
        ccList.add(recipient2.getUserEmailAddress());
        //Adding one person into the BCC list
        bccList.add(recipient3.getUserEmailAddress());
        //Adding a regular attachment to the attachment lists
        regularAttachments.add(new File("WindsorKen180.jpg"));
        //Adding an embedded attachment to the embedded attachment lists
        //embeddedAttachments.add(new File("FreeFall.jpg"));
        embeddedAttachments.add(new File("FreeFall.jpg"));
        //Instantiate a SendAndReceive object to utilize its methods
        runMail = new SendAndReceive(mailConfigBean);
        Email resultEmail = runMail.sendEmail(toList, ccList, bccList, subject, plainMsg, htmlMsg, regularAttachments, embeddedAttachments);

        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //since we are sending it, it does not have a receivedDate so we leave it as null.
        EmailBean emailBean = new EmailBean(-1, 2, null, resultEmail);
        int result = crud.createEmailRecord(emailBean);
        EmailBean resultBean = crud.findID(emailBean.getId());
        assertEquals("Test findById :", emailBean, resultBean);
    }
    @Ignore
    @Test
    public void testFindAllInFolder() throws SQLException{
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        //Finding all emails in the SENT folder
        List<EmailBean> emailsInFolder = crud.findAllInFolder(2);
        assertEquals(emailsInFolder.size(), checkCountEmailsInFolder(2));
    }
    
    private int checkCountEmailsInFolder(int folderKey) throws SQLException {
        int countFound = -1;
        String countEmailsInFolder = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAIL WHERE FOLDERID = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(countEmailsInFolder);) {
            ps.setInt(1,folderKey);
            ResultSet countResult = ps.executeQuery();
            if(countResult.next()){
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }
    @Ignore
    @Test
    public void testFindBySender() throws SQLException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsBySender = crud.findEmailsBySender("danieldawsontest1@gmail.com");
        emailsBySender.forEach(bean -> {
            LOG.info("ID: " + bean.getId() + " send by " + bean.email.from().toString());
        });
        assertEquals(emailsBySender.size(), checkCountBySender("danieldawsontest1@gmail.com"));
    }
    private int checkCountBySender(String sender) throws SQLException {
        int countFound = -1;
        String countEmailsBySender = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAIL WHERE FROMADDRESS = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(countEmailsBySender);) {
            ps.setString(1,sender);
            ResultSet countResult = ps.executeQuery();
            if(countResult.next()){
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }
    @Ignore
    @Test
    public void testFindBySubject() throws SQLException {
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        List<EmailBean> emailsBySubject = crud.findEmailsBySubject("SQL TESTING");
        emailsBySubject.forEach(bean -> {
            LOG.info("ID: " + bean.getId() + " has subject:  " + bean.email.subject());
        });
        assertEquals(emailsBySubject.size(), checkCountBySubject("SQL TESTING"));
    }
    private int checkCountBySubject(String subject) throws SQLException {
        int countFound = -1;
        String countEmailsBySubject = "SELECT COUNT(*) AS 'EMAILCOUNT' FROM EMAIL WHERE SUBJECT = ?";
        try ( Connection connection = DriverManager.getConnection(mailConfigBean.getDatabaseUrl(), mailConfigBean.getDatabaseUserName(), mailConfigBean.getDatabasePassword());  
                PreparedStatement ps = connection.prepareStatement(countEmailsBySubject);) {
            ps.setString(1,subject);
            ResultSet countResult = ps.executeQuery();
            if(countResult.next()){
                countFound = countResult.getInt("EMAILCOUNT");
            }
        }
        return countFound;
    }
    @Ignore
    @Test
    public void testFolderCreation() throws SQLException{
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("MyImportantDocuments");
        assertEquals(1,foldersCreated);
    }
    @Ignore
    @Test(expected = IllegalArgumentException.class)
    public void testFolderAlreadyExists() throws SQLException{
        EmailDAO crud = new EmailDAOImpl(mailConfigBean);
        int foldersCreated = crud.createFolder("INBOX");
        fail("The folder was created.");
    }
    @Test
    public void testUpdateDraftEmail() throws SQLException {
         EmailDAO crud = new EmailDAOImpl(mailConfigBean);
         EmailBean emailBean = crud.findID(4);
         LOG.info("BEFORE CHANGES SUBJECT: " + emailBean.email.subject());
         LOG.info("BEFORE CHANGES FROM: " + emailBean.email.from().toString());
         emailBean.email.subject("NEW SUBJECT");
         emailBean.email.from("newsender1@gmail.com");
         emailBean.email.to("newrecipient1");
         emailBean.email.cc("newrecipient2");
         emailBean.email.bcc("newrecipient3");
         int resultUpdates = crud.updateDraft(emailBean);
         assertEquals(1,resultUpdates);

    }

    

    

}
